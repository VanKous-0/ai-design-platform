package com.project.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AdminBootstrapProperties properties;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(
            AdminBootstrapProperties properties,
            SysUserMapper sysUserMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getUsername()) && !StringUtils.hasText(properties.getPassword())) {
            return;
        }
        if (!StringUtils.hasText(properties.getUsername()) || !StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_USERNAME和ADMIN_BOOTSTRAP_PASSWORD必须同时配置");
        }
        if (properties.getPassword().length() < 12) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD至少需要12个字符");
        }

        String username = properties.getUsername().trim();
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (existing != null) {
            log.info("Bootstrap admin '{}' already exists; password was not changed", username);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        admin.setNickname(StringUtils.hasText(properties.getNickname())
                ? properties.getNickname().trim()
                : "系统管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreateTime(now);
        admin.setUpdateTime(now);
        admin.setIsDeleted(0);
        sysUserMapper.insert(admin);
        log.info("Bootstrap admin '{}' created", username);
    }
}
