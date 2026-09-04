package com.project.modules.profile.service;

import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.vo.UserPreferenceSignalVO;

import java.util.List;

public interface UserPreferenceSignalService {

    UserPreferenceSignalVO upsert(Long userId, UserPreferenceSignalUpsertRequest request);

    UserPreferenceSignalVO upsertUserDeclared(Long userId, UserPreferenceSignalUpsertRequest request);

    UserPreferenceSignalVO upsertInferred(Long userId, UserPreferenceSignalUpsertRequest request);

    List<UserPreferenceSignalVO> listAll(Long userId);

    List<UserPreferenceSignalVO> listEffective(Long userId);

    void replaceUserDeclared(Long userId, String preferenceKey, String preferenceValue);
}
