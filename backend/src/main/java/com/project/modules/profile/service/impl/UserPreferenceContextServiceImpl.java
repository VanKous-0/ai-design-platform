package com.project.modules.profile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.modules.profile.model.ProfileContextPreference;
import com.project.modules.profile.model.ProfileContextSnapshot;
import com.project.modules.profile.service.UserPreferenceContextService;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class UserPreferenceContextServiceImpl implements UserPreferenceContextService {

    private static final int SNAPSHOT_SCHEMA_VERSION = 1;

    private final UserPreferenceSignalService preferenceSignalService;
    private final ObjectMapper objectMapper;

    public UserPreferenceContextServiceImpl(
            UserPreferenceSignalService preferenceSignalService,
            ObjectMapper objectMapper
    ) {
        this.preferenceSignalService = preferenceSignalService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<UserPreferenceSignalVO> getEffectiveContext(Long userId) {
        return preferenceSignalService.listEffective(userId).stream()
                .sorted(Comparator
                        .comparing(UserPreferenceSignalVO::getScope)
                        .thenComparing(UserPreferenceSignalVO::getSentiment)
                        .thenComparing(UserPreferenceSignalVO::getPreferenceKey)
                        .thenComparing(UserPreferenceSignalVO::getSource)
                        .thenComparing(UserPreferenceSignalVO::getId))
                .toList();
    }

    @Override
    public String buildContextSnapshot(Long userId) {
        List<ProfileContextPreference> preferences = getEffectiveContext(userId).stream()
                .map(signal -> new ProfileContextPreference(
                        signal.getId(),
                        signal.getPreferenceKey(),
                        signal.getPreferenceValue(),
                        signal.getSentiment(),
                        signal.getScope(),
                        signal.getSource(),
                        signal.getConfidence(),
                        signal.getLastObservedAt()
                ))
                .toList();
        try {
            return objectMapper.writeValueAsString(
                    new ProfileContextSnapshot(SNAPSHOT_SCHEMA_VERSION, preferences)
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize effective preference context", ex);
        }
    }
}
