package com.project.modules.statistics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.BusinessException;
import com.project.modules.statistics.dto.UsageEventCreateRequest;
import com.project.modules.statistics.entity.UsageEvent;
import com.project.modules.statistics.mapper.UsageEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceImplTest {

    @Mock
    private UsageEventMapper usageEventMapper;

    private UsageEventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsageEventServiceImpl(usageEventMapper, new ObjectMapper());
    }

    @Test
    void normalizesAndStoresAllowedEvent() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType(" RENDER_PROMPT ");
        request.setTargetType(" PROMPT ");
        request.setTargetId(8L);
        request.setExtraJson("{\"source\":\"workflow\"}");

        service.createEvent(7L, request);

        verify(usageEventMapper).insert(any(UsageEvent.class));
        assertEquals("render_prompt", request.getEventType().trim().toLowerCase());
    }

    @Test
    void rejectsUnknownEventType() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType("random_click");

        assertThrows(BusinessException.class, () -> service.createEvent(7L, request));
    }

    @Test
    void rejectsInvalidExtraJson() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType("login");
        request.setExtraJson("not-json");

        assertThrows(BusinessException.class, () -> service.createEvent(7L, request));
    }
}
