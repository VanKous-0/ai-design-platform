package com.project.modules.profile.service;

import com.project.modules.profile.vo.UserPreferenceSignalVO;

import java.util.List;

public interface UserPreferenceContextService {

    List<UserPreferenceSignalVO> getEffectiveContext(Long userId);

    String buildContextSnapshot(Long userId);
}
