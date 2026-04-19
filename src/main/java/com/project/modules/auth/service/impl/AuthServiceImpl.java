package com.project.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.config.JwtProperties;
import com.project.common.exception.BusinessException;
import com.project.common.util.JwtUtil;
import com.project.modules.auth.dto.LoginRequest;
import com.project.modules.auth.service.AuthService;
import com.project.modules.auth.vo.LoginResponse;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import com.project.modules.user.vo.UserInfoVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(
            SysUserMapper sysUserMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            JwtProperties jwtProperties
    ) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("limit 1"));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .tokenType(jwtProperties.getTokenPrefix())
                .accessToken(token)
                .expiresIn(jwtProperties.getExpirationSeconds())
                .user(UserInfoVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
