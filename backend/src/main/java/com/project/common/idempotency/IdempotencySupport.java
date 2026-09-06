package com.project.common.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class IdempotencySupport {

    public static final int MAX_KEY_LENGTH = 100;

    private final ObjectMapper objectMapper;

    public IdempotencySupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String normalizeKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        String normalized = key.trim();
        if (normalized.length() > MAX_KEY_LENGTH) {
            throw new DomainException(DomainError.VALIDATION_ERROR,
                    "Idempotency-Key cannot exceed " + MAX_KEY_LENGTH + " characters");
        }
        return normalized;
    }

    public String fingerprint(Object requestShape) {
        try {
            byte[] canonicalBytes = objectMapper.writeValueAsString(requestShape)
                    .getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonicalBytes));
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to fingerprint idempotent request", ex);
        }
    }

    public void requireSameRequest(String storedFingerprint, String actualFingerprint) {
        if (!actualFingerprint.equals(storedFingerprint)) {
            throw new DomainException(DomainError.RESOURCE_CONFLICT,
                    "Idempotency-Key was already used for a different request");
        }
    }
}
