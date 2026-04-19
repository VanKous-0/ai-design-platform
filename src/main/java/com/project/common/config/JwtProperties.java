package com.project.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String header = "Authorization";

    private String tokenPrefix = "Bearer";

    private String secret;

    private Long expirationSeconds = 86400L;
}
