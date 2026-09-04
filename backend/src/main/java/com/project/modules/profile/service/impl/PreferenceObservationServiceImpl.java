package com.project.modules.profile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.model.PreferenceEvidence;
import com.project.modules.profile.model.PreferenceScope;
import com.project.modules.profile.model.PreferenceSentiment;
import com.project.modules.profile.model.PreferenceSource;
import com.project.modules.profile.service.PreferenceObservationService;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.prompt.entity.PromptPreferenceHint;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.mapper.PromptPreferenceHintMapper;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PreferenceObservationServiceImpl implements PreferenceObservationService {

    private final PromptPreferenceHintMapper hintMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final UserPreferenceSignalService preferenceSignalService;

    public PreferenceObservationServiceImpl(
            PromptPreferenceHintMapper hintMapper,
            PromptTemplateMapper promptTemplateMapper,
            UserPreferenceSignalService preferenceSignalService
    ) {
        this.hintMapper = hintMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.preferenceSignalService = preferenceSignalService;
    }

    @Override
    public List<PreferenceEvidence> resolvePromptRenderEvidence(Long promptId) {
        if (promptId == null) {
            return List.of();
        }
        PromptTemplate prompt = promptTemplateMapper.selectById(promptId);
        if (prompt == null || !Integer.valueOf(1).equals(prompt.getStatus())) {
            return List.of();
        }
        return hintMapper.selectList(new LambdaQueryWrapper<PromptPreferenceHint>()
                        .eq(PromptPreferenceHint::getPromptId, promptId)
                        .orderByAsc(PromptPreferenceHint::getPreferenceKey)
                        .orderByAsc(PromptPreferenceHint::getId))
                .stream()
                .map(hint -> new PreferenceEvidence(
                        hint.getId(),
                        hint.getPromptId(),
                        hint.getPreferenceKey(),
                        hint.getPreferenceValue()
                ))
                .toList();
    }

    @Override
    public void observe(
            Long userId,
            Long usageEventId,
            LocalDateTime observedAt,
            List<PreferenceEvidence> evidence
    ) {
        if (userId == null || evidence == null || evidence.isEmpty()) {
            return;
        }
        for (PreferenceEvidence item : evidence) {
            UserPreferenceSignalUpsertRequest request = new UserPreferenceSignalUpsertRequest();
            request.setPreferenceKey(item.preferenceKey());
            request.setPreferenceValue(item.preferenceValue());
            request.setSentiment(PreferenceSentiment.PREFER.name());
            request.setScope(PreferenceScope.LONG_TERM.name());
            request.setSource(PreferenceSource.BEHAVIOR_INFERRED.name());
            request.setEvidenceSummary(
                    "UsageEvent #" + usageEventId
                            + " rendered Prompt #" + item.promptId()
                            + " using explicit mapping #" + item.mappingId()
            );
            request.setObservedAt(observedAt);
            preferenceSignalService.upsertInferred(userId, request);
        }
    }
}
