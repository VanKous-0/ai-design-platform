package com.project.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public class AdminBootstrapProperties {

    private String username;

    private String password;

    private String nickname = "系统管理员";
}
