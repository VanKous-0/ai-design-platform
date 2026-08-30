package com.project.modules.profile.service;

import com.project.modules.profile.dto.UserDesignPreferenceUpdateRequest;
import com.project.modules.profile.dto.UserProfileUpdateRequest;
import com.project.modules.profile.dto.UserRecentParameterCreateRequest;
import com.project.modules.profile.vo.UserDesignPreferenceVO;
import com.project.modules.profile.vo.UserProfileVO;
import com.project.modules.profile.vo.UserRecentParameterVO;

import java.util.List;

public interface UserProfileService {

    UserProfileVO getProfile(Long userId);

    UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request);

    UserDesignPreferenceVO getPreference(Long userId);

    UserDesignPreferenceVO updatePreference(Long userId, UserDesignPreferenceUpdateRequest request);

    List<UserRecentParameterVO> listRecentParameters(Long userId);

    UserRecentParameterVO saveRecentParameter(Long userId, UserRecentParameterCreateRequest request);

    void deleteRecentParameter(Long userId, Long id);
}
