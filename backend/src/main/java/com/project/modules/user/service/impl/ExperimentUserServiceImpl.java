package com.project.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.user.dto.ExperimentUserBatchCreateRequest;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import com.project.modules.user.service.ExperimentUserService;
import com.project.modules.user.vo.ExperimentUserCredentialVO;
import com.project.modules.user.vo.ExperimentUserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExperimentUserServiceImpl implements ExperimentUserService {

    private static final String USER_ROLE = "USER";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public ExperimentUserServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ExperimentUserCredentialVO> createBatch(ExperimentUserBatchCreateRequest request) {
        int startNumber = request.getStartNumber() == null ? 1 : request.getStartNumber();
        String usernamePrefix = request.getUsernamePrefix().trim();
        String codePrefix = StringUtils.hasText(request.getExperimentCodePrefix())
                ? request.getExperimentCodePrefix().trim()
                : usernamePrefix;
        String batch = request.getExperimentBatch().trim();
        String group = request.getExperimentGroup().trim();

        List<String> usernames = new ArrayList<>();
        List<String> experimentCodes = new ArrayList<>();
        for (int offset = 0; offset < request.getCount(); offset++) {
            int sequence = startNumber + offset;
            usernames.add(formatIdentifier(usernamePrefix, sequence));
            experimentCodes.add(formatIdentifier(codePrefix, sequence));
        }
        ensureIdentifiersAvailable(usernames, experimentCodes);

        String encodedPassword = passwordEncoder.encode(request.getInitialPassword());
        LocalDateTime now = LocalDateTime.now();
        List<ExperimentUserCredentialVO> created = new ArrayList<>();
        for (int index = 0; index < usernames.size(); index++) {
            SysUser user = new SysUser();
            user.setUsername(usernames.get(index));
            user.setPasswordHash(encodedPassword);
            user.setNickname(experimentCodes.get(index));
            user.setRole(USER_ROLE);
            user.setStatus(1);
            user.setExperimentCode(experimentCodes.get(index));
            user.setExperimentGroup(group);
            user.setExperimentBatch(batch);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user.setIsDeleted(0);
            sysUserMapper.insert(user);

            created.add(ExperimentUserCredentialVO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .experimentCode(user.getExperimentCode())
                    .experimentGroup(group)
                    .experimentBatch(batch)
                    .initialPassword(request.getInitialPassword())
                    .build());
        }
        return created;
    }

    @Override
    public PageResult<ExperimentUserVO> pageUsers(
            String experimentBatch,
            String experimentGroup,
            String keyword,
            Long pageNum,
            Long pageSize
    ) {
        Page<SysUser> page = PageSupport.page(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, USER_ROLE)
                .isNotNull(SysUser::getExperimentCode)
                .eq(StringUtils.hasText(experimentBatch), SysUser::getExperimentBatch, trim(experimentBatch))
                .eq(StringUtils.hasText(experimentGroup), SysUser::getExperimentGroup, trim(experimentGroup))
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysUser::getUsername, trim(keyword))
                        .or()
                        .like(SysUser::getExperimentCode, trim(keyword)))
                .orderByDesc(SysUser::getCreateTime)
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = sysUserMapper.selectPage(page, query);
        return PageResult.<ExperimentUserVO>builder()
                .records(result.getRecords().stream().map(this::toVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    @Override
    public ExperimentUserVO updateStatus(Long userId, Integer status) {
        SysUser user = getExperimentUser(userId);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = getExperimentUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    private void ensureIdentifiersAvailable(List<String> usernames, List<String> experimentCodes) {
        Long usernameCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getUsername, usernames));
        if (usernameCount > 0) {
            throw new BusinessException("生成的用户名已存在，请调整前缀或起始序号");
        }
        Long codeCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getExperimentCode, experimentCodes));
        if (codeCount > 0) {
            throw new BusinessException("生成的实验编号已存在，请调整前缀或起始序号");
        }
    }

    private SysUser getExperimentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !USER_ROLE.equals(user.getRole()) || !StringUtils.hasText(user.getExperimentCode())) {
            throw new BusinessException("实验账号不存在");
        }
        return user;
    }

    private String formatIdentifier(String prefix, int sequence) {
        return prefix + String.format("%03d", sequence);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private ExperimentUserVO toVO(SysUser user) {
        return ExperimentUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .experimentCode(user.getExperimentCode())
                .experimentGroup(user.getExperimentGroup())
                .experimentBatch(user.getExperimentBatch())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
