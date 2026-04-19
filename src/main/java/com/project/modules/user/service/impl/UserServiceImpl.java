package com.project.modules.user.service.impl;

import com.project.common.exception.BusinessException;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import com.project.modules.user.service.UserService;
import com.project.modules.user.vo.UserInfoVO;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已被禁用");
        }
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }
}
