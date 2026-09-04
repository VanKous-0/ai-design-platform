package com.project.modules.profile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.entity.UserPreferenceSignal;
import com.project.modules.profile.mapper.UserPreferenceSignalMapper;
import com.project.modules.profile.model.PreferenceScope;
import com.project.modules.profile.model.PreferenceSentiment;
import com.project.modules.profile.model.PreferenceSource;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPreferenceSignalServiceImpl implements UserPreferenceSignalService {

    private static final BigDecimal DECLARED_CONFIDENCE = new BigDecimal("1.000");
    private static final BigDecimal INITIAL_BEHAVIOR_CONFIDENCE = new BigDecimal("0.300");
    private static final BigDecimal MAX_BEHAVIOR_CONFIDENCE = new BigDecimal("0.800");
    private static final BigDecimal DEFAULT_AGENT_CONFIDENCE = new BigDecimal("0.600");
    private static final BigDecimal REPEATED_OBSERVATION_INCREMENT = new BigDecimal("0.100");

    private final UserPreferenceSignalMapper signalMapper;

    public UserPreferenceSignalServiceImpl(UserPreferenceSignalMapper signalMapper) {
        this.signalMapper = signalMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPreferenceSignalVO upsertUserDeclared(Long userId, UserPreferenceSignalUpsertRequest request) {
        PreferenceSource source = parseEnum(
                PreferenceSource.class, request.getSource(), "Unsupported preference source"
        );
        if (source != PreferenceSource.USER_DECLARED) {
            throw new BusinessException("The user preference endpoint only accepts USER_DECLARED signals");
        }
        return upsert(userId, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPreferenceSignalVO upsertInferred(Long userId, UserPreferenceSignalUpsertRequest request) {
        PreferenceSource source = parseEnum(
                PreferenceSource.class, request.getSource(), "Unsupported preference source"
        );
        if (source == PreferenceSource.USER_DECLARED) {
            throw new BusinessException("USER_DECLARED signals must be submitted by the profile owner");
        }
        return upsert(userId, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPreferenceSignalVO upsert(Long userId, UserPreferenceSignalUpsertRequest request) {
        PreferenceSource source = parseEnum(PreferenceSource.class, request.getSource(), "Unsupported preference source");
        PreferenceScope scope = parseEnum(PreferenceScope.class, request.getScope(), "Unsupported preference scope");
        PreferenceSentiment sentiment = StringUtils.hasText(request.getSentiment())
                ? parseEnum(PreferenceSentiment.class, request.getSentiment(), "Unsupported preference sentiment")
                : PreferenceSentiment.PREFER;
        String key = request.getPreferenceKey().trim();
        String value = request.getPreferenceValue().trim();
        LocalDateTime observedAt = request.getObservedAt() == null ? LocalDateTime.now() : request.getObservedAt();

        UserPreferenceSignal signal = signalMapper.selectOne(new LambdaQueryWrapper<UserPreferenceSignal>()
                .eq(UserPreferenceSignal::getUserId, userId)
                .eq(UserPreferenceSignal::getPreferenceKey, key)
                .eq(UserPreferenceSignal::getSentiment, sentiment.name())
                .eq(UserPreferenceSignal::getScope, scope.name())
                .eq(UserPreferenceSignal::getSource, source.name())
                .last("limit 1 FOR UPDATE"));

        LocalDateTime now = LocalDateTime.now();
        if (signal == null) {
            signal = new UserPreferenceSignal();
            signal.setUserId(userId);
            signal.setPreferenceKey(key);
            signal.setSentiment(sentiment.name());
            signal.setScope(scope.name());
            signal.setSource(source.name());
            signal.setEvidenceCount(1);
            signal.setCreateTime(now);
            signal.setIsDeleted(0);
        } else if (source == PreferenceSource.BEHAVIOR_INFERRED
                && !signal.getPreferenceValue().equals(value)) {
            signal.setEvidenceCount(1);
        } else if (source != PreferenceSource.USER_DECLARED) {
            signal.setEvidenceCount(signal.getEvidenceCount() + 1);
        }

        signal.setPreferenceValue(value);
        signal.setConfidence(resolveConfidence(signal, source, request.getConfidence()));
        signal.setEvidenceSummary(trimToNull(request.getEvidenceSummary()));
        signal.setLastObservedAt(observedAt);
        signal.setUpdateTime(now);
        if (signal.getId() == null) {
            signalMapper.insert(signal);
        } else {
            signalMapper.updateById(signal);
        }
        return toVO(signal);
    }

    @Override
    public List<UserPreferenceSignalVO> listAll(Long userId) {
        return signalMapper.selectList(new LambdaQueryWrapper<UserPreferenceSignal>()
                        .eq(UserPreferenceSignal::getUserId, userId)
                        .orderByAsc(UserPreferenceSignal::getScope)
                        .orderByAsc(UserPreferenceSignal::getPreferenceKey)
                        .orderByDesc(UserPreferenceSignal::getLastObservedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<UserPreferenceSignalVO> listEffective(Long userId) {
        Map<String, UserPreferenceSignalVO> effective = new LinkedHashMap<>();
        List<UserPreferenceSignalVO> ranked = new ArrayList<>(listAll(userId));
        ranked.sort(Comparator
                .comparingInt((UserPreferenceSignalVO signal) -> sourcePriority(signal.getSource())).reversed()
                .thenComparing(UserPreferenceSignalVO::getConfidence, Comparator.reverseOrder())
                .thenComparing(UserPreferenceSignalVO::getLastObservedAt, Comparator.reverseOrder()));
        for (UserPreferenceSignalVO signal : ranked) {
            String key = signal.getScope() + ":" + signal.getSentiment() + ":" + signal.getPreferenceKey();
            effective.putIfAbsent(key, signal);
        }
        return List.copyOf(effective.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceUserDeclared(Long userId, String preferenceKey, String preferenceValue) {
        UserPreferenceSignal existing = signalMapper.selectOne(new LambdaQueryWrapper<UserPreferenceSignal>()
                .eq(UserPreferenceSignal::getUserId, userId)
                .eq(UserPreferenceSignal::getPreferenceKey, preferenceKey)
                .eq(UserPreferenceSignal::getSentiment, PreferenceSentiment.PREFER.name())
                .eq(UserPreferenceSignal::getScope, PreferenceScope.LONG_TERM.name())
                .eq(UserPreferenceSignal::getSource, PreferenceSource.USER_DECLARED.name())
                .last("limit 1 FOR UPDATE"));
        if (!StringUtils.hasText(preferenceValue)) {
            if (existing != null) {
                signalMapper.hardDeleteById(existing.getId());
            }
            return;
        }

        UserPreferenceSignalUpsertRequest request = new UserPreferenceSignalUpsertRequest();
        request.setPreferenceKey(preferenceKey);
        request.setPreferenceValue(preferenceValue);
        request.setSentiment(PreferenceSentiment.PREFER.name());
        request.setScope(PreferenceScope.LONG_TERM.name());
        request.setSource(PreferenceSource.USER_DECLARED.name());
        request.setConfidence(DECLARED_CONFIDENCE);
        request.setEvidenceSummary("Updated through the legacy design preference API");
        upsert(userId, request);
    }

    private BigDecimal resolveConfidence(
            UserPreferenceSignal signal,
            PreferenceSource source,
            BigDecimal requestedConfidence
    ) {
        if (source == PreferenceSource.USER_DECLARED) {
            return DECLARED_CONFIDENCE;
        }
        if (source == PreferenceSource.BEHAVIOR_INFERRED) {
            if (signal.getId() == null || signal.getEvidenceCount() == 1) {
                return INITIAL_BEHAVIOR_CONFIDENCE;
            }
            return signal.getConfidence()
                    .add(REPEATED_OBSERVATION_INCREMENT)
                    .min(MAX_BEHAVIOR_CONFIDENCE)
                    .setScale(3, RoundingMode.HALF_UP);
        }
        return (requestedConfidence == null ? DEFAULT_AGENT_CONFIDENCE : requestedConfidence)
                .setScale(3, RoundingMode.HALF_UP);
    }

    private int sourcePriority(String source) {
        return switch (PreferenceSource.valueOf(source)) {
            case USER_DECLARED -> 3;
            case AGENT_INFERRED -> 2;
            case BEHAVIOR_INFERRED -> 1;
        };
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String value, String message) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessException(message);
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UserPreferenceSignalVO toVO(UserPreferenceSignal signal) {
        return UserPreferenceSignalVO.builder()
                .id(signal.getId())
                .userId(signal.getUserId())
                .preferenceKey(signal.getPreferenceKey())
                .preferenceValue(signal.getPreferenceValue())
                .sentiment(signal.getSentiment())
                .scope(signal.getScope())
                .source(signal.getSource())
                .confidence(signal.getConfidence())
                .evidenceCount(signal.getEvidenceCount())
                .evidenceSummary(signal.getEvidenceSummary())
                .lastObservedAt(signal.getLastObservedAt())
                .createTime(signal.getCreateTime())
                .updateTime(signal.getUpdateTime())
                .build();
    }
}
