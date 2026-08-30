package com.project.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String header = "Authorization";

    private String tokenPrefix = "Bearer";

    @NotBlank(message = "JWT_SECRET must be configured")
    @Size(min = 32, message = "JWT_SECRET must contain at least 32 characters")
    private String secret;

    @Positive(message = "JWT expiration must be positive")
    private Long expirationSeconds = 86400L;
}
