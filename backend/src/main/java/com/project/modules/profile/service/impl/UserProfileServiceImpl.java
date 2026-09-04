package com.project.modules.profile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.profile.dto.UserDesignPreferenceUpdateRequest;
import com.project.modules.profile.dto.UserProfileUpdateRequest;
import com.project.modules.profile.dto.UserRecentParameterCreateRequest;
import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.entity.UserDesignPreference;
import com.project.modules.profile.entity.UserProfile;
import com.project.modules.profile.entity.UserRecentParameter;
import com.project.modules.profile.mapper.UserDesignPreferenceMapper;
import com.project.modules.profile.mapper.UserProfileMapper;
import com.project.modules.profile.mapper.UserRecentParameterMapper;
import com.project.modules.profile.service.UserProfileService;
import com.project.modules.profile.service.UserPreferenceContextService;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.profile.vo.UserDesignPreferenceVO;
import com.project.modules.profile.vo.UserProfileVO;
import com.project.modules.profile.vo.UserRecentParameterVO;
import com.project.modules.profile.vo.UserPreferenceContextVO;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileMapper userProfileMapper;
    private final UserDesignPreferenceMapper preferenceMapper;
    private final UserRecentParameterMapper recentParameterMapper;
    private final AiToolMapper aiToolMapper;
    private final UserPreferenceSignalService preferenceSignalService;
    private final UserPreferenceContextService preferenceContextService;

    public UserProfileServiceImpl(
            UserProfileMapper userProfileMapper,
            UserDesignPreferenceMapper preferenceMapper,
            UserRecentParameterMapper recentParameterMapper,
            AiToolMapper aiToolMapper,
            UserPreferenceSignalService preferenceSignalService,
            UserPreferenceContextService preferenceContextService
    ) {
        this.userProfileMapper = userProfileMapper;
        this.preferenceMapper = preferenceMapper;
        this.recentParameterMapper = recentParameterMapper;
        this.aiToolMapper = aiToolMapper;
        this.preferenceSignalService = preferenceSignalService;
        this.preferenceContextService = preferenceContextService;
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        UserProfile profile = findProfile(userId);
        return profile == null ? emptyProfile(userId) : toProfileVO(profile);
    }

    @Override
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserProfile profile = findProfile(userId);
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreateTime(now);
            profile.setIsDeleted(0);
        }
        profile.setRealName(request.getRealName());
        profile.setSchool(request.getSchool());
        profile.setMajor(request.getMajor());
        profile.setGrade(request.getGrade());
        profile.setPhone(request.getPhone());
        profile.setBio(request.getBio());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setUpdateTime(now);
        if (profile.getId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        return toProfileVO(profile);
    }

    @Override
    public UserDesignPreferenceVO getPreference(Long userId) {
        UserDesignPreference preference = findPreference(userId);
        return preference == null ? emptyPreference(userId) : toPreferenceVO(preference);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDesignPreferenceVO updatePreference(Long userId, UserDesignPreferenceUpdateRequest request) {
        if (request.getDefaultToolId() != null) {
            ensureToolExists(request.getDefaultToolId());
        }
        UserDesignPreference preference = findPreference(userId);
        LocalDateTime now = LocalDateTime.now();
        if (preference == null) {
            preference = new UserDesignPreference();
            preference.setUserId(userId);
            preference.setCreateTime(now);
            preference.setIsDeleted(0);
        }
        preference.setPreferredProjectType(request.getPreferredProjectType());
        preference.setPreferredStyle(request.getPreferredStyle());
        preference.setPreferredSiteScale(request.getPreferredSiteScale());
        preference.setPreferredTargetUser(request.getPreferredTargetUser());
        preference.setDefaultToolId(request.getDefaultToolId());
        preference.setExtraJson(request.getExtraJson());
        preference.setUpdateTime(now);
        if (preference.getId() == null) {
            preferenceMapper.insert(preference);
        } else {
            preferenceMapper.updateById(preference);
        }
        preferenceSignalService.replaceUserDeclared(userId, "project_type", request.getPreferredProjectType());
        preferenceSignalService.replaceUserDeclared(userId, "style", request.getPreferredStyle());
        preferenceSignalService.replaceUserDeclared(userId, "site_scale", request.getPreferredSiteScale());
        preferenceSignalService.replaceUserDeclared(userId, "target_user", request.getPreferredTargetUser());
        preferenceSignalService.replaceUserDeclared(
                userId,
                "default_tool_id",
                request.getDefaultToolId() == null ? null : request.getDefaultToolId().toString()
        );
        return toPreferenceVO(preference);
    }

    @Override
    public UserPreferenceSignalVO upsertPreferenceSignal(Long userId, UserPreferenceSignalUpsertRequest request) {
        return preferenceSignalService.upsertUserDeclared(userId, request);
    }

    @Override
    public UserPreferenceContextVO getPreferenceContext(Long userId) {
        return UserPreferenceContextVO.builder()
                .userId(userId)
                .legacyPreference(getPreference(userId))
                .effectiveSignals(preferenceContextService.getEffectiveContext(userId))
                .allSignals(preferenceSignalService.listAll(userId))
                .build();
    }

    @Override
    public List<UserRecentParameterVO> listRecentParameters(Long userId) {
        return recentParameterMapper.selectList(new LambdaQueryWrapper<UserRecentParameter>()
                        .eq(UserRecentParameter::getUserId, userId)
                        .orderByDesc(UserRecentParameter::getLastUsedTime)
                        .orderByDesc(UserRecentParameter::getUseCount)
                        .orderByDesc(UserRecentParameter::getId))
                .stream()
                .map(this::toRecentParameterVO)
                .toList();
    }

    @Override
    public UserRecentParameterVO saveRecentParameter(Long userId, UserRecentParameterCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UserRecentParameter existing = recentParameterMapper.selectOne(new LambdaQueryWrapper<UserRecentParameter>()
                .eq(UserRecentParameter::getUserId, userId)
                .eq(UserRecentParameter::getParameterKey, request.getParameterKey())
                .eq(UserRecentParameter::getParameterValue, request.getParameterValue())
                .last("limit 1"));
        if (existing != null) {
            recentParameterMapper.update(null, new LambdaUpdateWrapper<UserRecentParameter>()
                    .eq(UserRecentParameter::getId, existing.getId())
                    .setSql("use_count = use_count + 1")
                    .set(UserRecentParameter::getParameterType, request.getParameterType())
                    .set(UserRecentParameter::getSource, request.getSource())
                    .set(UserRecentParameter::getLastUsedTime, now)
                    .set(UserRecentParameter::getUpdateTime, now));
            existing.setParameterType(request.getParameterType());
            existing.setSource(request.getSource());
            existing.setUseCount(existing.getUseCount() + 1);
            existing.setLastUsedTime(now);
            existing.setUpdateTime(now);
            return toRecentParameterVO(existing);
        }

        UserRecentParameter parameter = new UserRecentParameter();
        parameter.setUserId(userId);
        parameter.setParameterType(request.getParameterType());
        parameter.setParameterKey(request.getParameterKey());
        parameter.setParameterValue(request.getParameterValue());
        parameter.setSource(request.getSource());
        parameter.setUseCount(1);
        parameter.setLastUsedTime(now);
        parameter.setCreateTime(now);
        parameter.setUpdateTime(now);
        parameter.setIsDeleted(0);
        recentParameterMapper.insert(parameter);
        return toRecentParameterVO(parameter);
    }

    @Override
    public void deleteRecentParameter(Long userId, Long id) {
        UserRecentParameter parameter = recentParameterMapper.selectOne(new LambdaQueryWrapper<UserRecentParameter>()
                .eq(UserRecentParameter::getId, id)
                .eq(UserRecentParameter::getUserId, userId)
                .last("limit 1"));
        if (parameter == null) {
            throw new BusinessException("Recent parameter does not exist or you have no permission");
        }
        recentParameterMapper.deleteById(id);
    }

    private UserProfile findProfile(Long userId) {
        return userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .last("limit 1"));
    }

    private UserDesignPreference findPreference(Long userId) {
        return preferenceMapper.selectOne(new LambdaQueryWrapper<UserDesignPreference>()
                .eq(UserDesignPreference::getUserId, userId)
                .last("limit 1"));
    }

    private void ensureToolExists(Long toolId) {
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null) {
            throw new BusinessException("AI tool does not exist");
        }
    }

    private UserProfileVO emptyProfile(Long userId) {
        return UserProfileVO.builder().userId(userId).build();
    }

    private UserDesignPreferenceVO emptyPreference(Long userId) {
        return UserDesignPreferenceVO.builder().userId(userId).build();
    }

    private UserProfileVO toProfileVO(UserProfile profile) {
        return UserProfileVO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .realName(profile.getRealName())
                .school(profile.getSchool())
                .major(profile.getMajor())
                .grade(profile.getGrade())
                .phone(profile.getPhone())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .createTime(profile.getCreateTime())
                .updateTime(profile.getUpdateTime())
                .build();
    }

    private UserDesignPreferenceVO toPreferenceVO(UserDesignPreference preference) {
        return UserDesignPreferenceVO.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .preferredProjectType(preference.getPreferredProjectType())
                .preferredStyle(preference.getPreferredStyle())
                .preferredSiteScale(preference.getPreferredSiteScale())
                .preferredTargetUser(preference.getPreferredTargetUser())
                .defaultToolId(preference.getDefaultToolId())
                .extraJson(preference.getExtraJson())
                .createTime(preference.getCreateTime())
                .updateTime(preference.getUpdateTime())
                .build();
    }

    private UserRecentParameterVO toRecentParameterVO(UserRecentParameter parameter) {
        return UserRecentParameterVO.builder()
                .id(parameter.getId())
                .userId(parameter.getUserId())
                .parameterType(parameter.getParameterType())
                .parameterKey(parameter.getParameterKey())
                .parameterValue(parameter.getParameterValue())
                .source(parameter.getSource())
                .useCount(parameter.getUseCount())
                .lastUsedTime(parameter.getLastUsedTime())
                .createTime(parameter.getCreateTime())
                .updateTime(parameter.getUpdateTime())
                .build();
    }
}
