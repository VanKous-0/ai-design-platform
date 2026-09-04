package com.project.modules.profile.service;

import com.project.modules.profile.model.PreferenceEvidence;

import java.time.LocalDateTime;
import java.util.List;

public interface PreferenceObservationService {

    List<PreferenceEvidence> resolvePromptRenderEvidence(Long promptId);

    void observe(
            Long userId,
            Long usageEventId,
            LocalDateTime observedAt,
            List<PreferenceEvidence> evidence
    );
}
