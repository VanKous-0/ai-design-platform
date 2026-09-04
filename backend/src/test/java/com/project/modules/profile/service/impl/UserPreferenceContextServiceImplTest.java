package com.project.modules.profile.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserPreferenceContextServiceImplTest {

    @Test
    void buildsVersionedSnapshotWithStablePreferenceOrder() throws Exception {
        UserPreferenceSignalService signalService = mock(UserPreferenceSignalService.class);
        LocalDateTime observedAt = LocalDateTime.of(2026, 9, 4, 12, 0);
        when(signalService.listEffective(7L)).thenReturn(List.of(
                signal(2L, "style", "新中式", observedAt),
                signal(1L, "project_type", "社区公园", observedAt)
        ));
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        UserPreferenceContextServiceImpl service = new UserPreferenceContextServiceImpl(
                signalService,
                objectMapper
        );

        JsonNode snapshot = objectMapper.readTree(service.buildContextSnapshot(7L));

        assertEquals(1, snapshot.path("schemaVersion").asInt());
        assertEquals(2, snapshot.path("preferences").size());
        assertEquals("project_type", snapshot.path("preferences").get(0).path("preferenceKey").asText());
        assertEquals("style", snapshot.path("preferences").get(1).path("preferenceKey").asText());
        assertEquals("USER_DECLARED", snapshot.path("preferences").get(1).path("source").asText());
        assertEquals(0, new BigDecimal("1.000").compareTo(snapshot.path("preferences").get(1)
                .path("confidence").decimalValue()));
    }

    private UserPreferenceSignalVO signal(
            Long id,
            String key,
            String value,
            LocalDateTime observedAt
    ) {
        return UserPreferenceSignalVO.builder()
                .id(id)
                .userId(7L)
                .preferenceKey(key)
                .preferenceValue(value)
                .sentiment("PREFER")
                .scope("LONG_TERM")
                .source("USER_DECLARED")
                .confidence(new BigDecimal("1.000"))
                .evidenceCount(1)
                .lastObservedAt(observedAt)
                .build();
    }
}
