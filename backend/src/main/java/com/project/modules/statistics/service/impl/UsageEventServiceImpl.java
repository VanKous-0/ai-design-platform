package com.project.modules.statistics.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.BusinessException;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import com.project.common.idempotency.IdempotencySupport;
import com.project.modules.profile.model.PreferenceEvidence;
import com.project.modules.profile.service.PreferenceObservationService;
import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.entity.UsageEvent;
import com.project.modules.statistics.mapper.UsageEventMapper;
import com.project.modules.statistics.service.UsageEventService;
import com.project.modules.statistics.support.ExperimentEventCatalog;
import com.project.modules.statistics.vo.UsageEventVO;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UsageEventServiceImpl implements UsageEventService {

    private final UsageEventMapper usageEventMapper;
    private final ObjectMapper objectMapper;
    private final PreferenceObservationService preferenceObservationService;
    private final IdempotencySupport idempotencySupport;

    public UsageEventServiceImpl(
            UsageEventMapper usageEventMapper,
            ObjectMapper objectMapper,
            PreferenceObservationService preferenceObservationService,
            IdempotencySupport idempotencySupport
    ) {
        this.usageEventMapper = usageEventMapper;
        this.objectMapper = objectMapper;
        this.preferenceObservationService = preferenceObservationService;
        this.idempotencySupport = idempotencySupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UsageEventVO createEvent(Long userId, UsageEventCreateRequest request, String idempotencyKey) {
        String eventType = request.getEventType().trim().toLowerCase();
        String targetType = trimToNull(request.getTargetType());
        if (targetType != null) {
            targetType = targetType.toLowerCase();
        }
        ExperimentEventCatalog.validate(eventType, targetType, request.getTargetId());
        validateExtraJson(request.getExtraJson());
        String anonymousId = trimToNull(request.getAnonymousId());
        String requestId = idempotencySupport.normalizeKey(idempotencyKey);
        String actor = resolveActor(userId, anonymousId, requestId);
        String requestHash = requestId == null ? null : eventFingerprint(eventType, targetType, request);
        UsageEvent replay = findReplay(actor, requestId, requestHash);
        if (replay != null) {
            return toVO(replay);
        }
        List<PreferenceEvidence> preferenceEvidence = resolvePreferenceEvidence(
                eventType, targetType, request.getTargetId()
        );

        LocalDateTime now = LocalDateTime.now();
        UsageEvent event = new UsageEvent();
        event.setUserId(userId);
        event.setAnonymousId(anonymousId);
        event.setEventType(eventType);
        event.setTargetType(targetType);
        event.setTargetId(request.getTargetId());
        event.setPageUrl(trimToNull(request.getPageUrl()));
        event.setStayDuration(request.getStayDuration());
        event.setInputSummary(trimToNull(request.getInputSummary()));
        event.setExtraJson(trimToNull(request.getExtraJson()));
        event.setPreferenceEvidenceJson(serializePreferenceEvidence(preferenceEvidence));
        event.setIdempotencyActor(actor);
        event.setIdempotencyKey(requestId);
        event.setIdempotencyRequestHash(requestHash);
        event.setCreateTime(now);
        event.setUpdateTime(now);
        event.setIsDeleted(0);
        try {
            usageEventMapper.insert(event);
        } catch (DuplicateKeyException ex) {
            replay = findReplay(actor, requestId, requestHash);
            if (replay != null) {
                return toVO(replay);
            }
            throw new DomainException(DomainError.RESOURCE_CONFLICT,
                    "Usage event conflicts with an existing idempotent request");
        }
        if (userId != null && !preferenceEvidence.isEmpty()) {
            preferenceObservationService.observe(userId, event.getId(), now, preferenceEvidence);
        }
        return toVO(event);
    }

    private UsageEvent findReplay(String actor, String requestId, String requestHash) {
        if (requestId == null) {
            return null;
        }
        UsageEvent existing = usageEventMapper.selectIdempotent(actor, requestId);
        if (existing != null) {
            idempotencySupport.requireSameRequest(existing.getIdempotencyRequestHash(), requestHash);
        }
        return existing;
    }

    private String resolveActor(Long userId, String anonymousId, String requestId) {
        if (requestId == null) {
            return null;
        }
        if (userId != null) {
            return "USER:" + userId;
        }
        if (anonymousId != null) {
            return "ANON:" + anonymousId;
        }
        throw new DomainException(DomainError.VALIDATION_ERROR,
                "Anonymous idempotent usage events require anonymousId");
    }

    private String eventFingerprint(String eventType, String targetType, UsageEventCreateRequest request) {
        Map<String, Object> shape = new LinkedHashMap<>();
        shape.put("eventType", eventType);
        shape.put("targetType", targetType);
        shape.put("targetId", request.getTargetId());
        shape.put("pageUrl", trimToNull(request.getPageUrl()));
        shape.put("stayDuration", request.getStayDuration());
        shape.put("inputSummary", trimToNull(request.getInputSummary()));
        shape.put("extraJson", trimToNull(request.getExtraJson()));
        return idempotencySupport.fingerprint(shape);
    }

    private List<PreferenceEvidence> resolvePreferenceEvidence(
            String eventType,
            String targetType,
            Long targetId
    ) {
        if (!ExperimentEventCatalog.RENDER_PROMPT.equals(eventType) || !"prompt".equals(targetType)) {
            return List.of();
        }
        return preferenceObservationService.resolvePromptRenderEvidence(targetId);
    }

    private String serializePreferenceEvidence(List<PreferenceEvidence> evidence) {
        if (evidence.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize server-resolved preference evidence", ex);
        }
    }

    private void validateExtraJson(String extraJson) {
        if (!StringUtils.hasText(extraJson)) {
            return;
        }
        try {
            if (!objectMapper.readTree(extraJson).isObject()) {
                throw new BusinessException("extraJson必须是JSON对象");
            }
        } catch (JsonProcessingException ex) {
            throw new BusinessException("extraJson不是合法JSON");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UsageEventVO toVO(UsageEvent event) {
        return UsageEventVO.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .anonymousId(event.getAnonymousId())
                .eventType(event.getEventType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .pageUrl(event.getPageUrl())
                .stayDuration(event.getStayDuration())
                .inputSummary(event.getInputSummary())
                .extraJson(event.getExtraJson())
                .preferenceEvidenceJson(event.getPreferenceEvidenceJson())
                .createTime(event.getCreateTime())
                .build();
    }
}
