package com.project.modules.profile.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserPreferenceContextVO {

    private Long userId;

    private UserDesignPreferenceVO legacyPreference;

    private List<UserPreferenceSignalVO> effectiveSignals;

    private List<UserPreferenceSignalVO> allSignals;
}
