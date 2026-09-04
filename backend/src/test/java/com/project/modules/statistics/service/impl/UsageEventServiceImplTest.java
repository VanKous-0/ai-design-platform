package com.project.modules.statistics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.BusinessException;
import com.project.modules.profile.model.PreferenceEvidence;
import com.project.modules.profile.service.PreferenceObservationService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceImplTest {

    @Mock
    private UsageEventMapper usageEventMapper;

    @Mock
    private PreferenceObservationService preferenceObservationService;

    private UsageEventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsageEventServiceImpl(
                usageEventMapper,
                new ObjectMapper(),
                preferenceObservationService
        );
    }

    @Test
    void normalizesAndStoresAllowedEvent() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType(" RENDER_PROMPT ");
        request.setTargetType(" PROMPT ");
        request.setTargetId(8L);
        request.setExtraJson("{\"source\":\"workflow\"}");
        PreferenceEvidence evidence = new PreferenceEvidence(3L, 8L, "style", "新中式");
        when(preferenceObservationService.resolvePromptRenderEvidence(8L)).thenReturn(java.util.List.of(evidence));
        doAnswer(invocation -> {
            UsageEvent event = invocation.getArgument(0);
            event.setId(100L);
            return 1;
        }).when(usageEventMapper).insert(any(UsageEvent.class));

        service.createEvent(7L, request);

        verify(usageEventMapper).insert(any(UsageEvent.class));
        verify(preferenceObservationService).observe(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(100L),
                any(),
                org.mockito.ArgumentMatchers.eq(java.util.List.of(evidence))
        );
        assertEquals("render_prompt", request.getEventType().trim().toLowerCase());
    }

    @Test
    void anonymousPromptRenderNeverUpdatesProfile() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType("render_prompt");
        request.setTargetType("prompt");
        request.setTargetId(8L);
        when(preferenceObservationService.resolvePromptRenderEvidence(8L)).thenReturn(java.util.List.of(
                new PreferenceEvidence(3L, 8L, "style", "新中式")
        ));

        service.createEvent(null, request);

        verify(preferenceObservationService, never()).observe(any(), any(), any(), any());
    }

    @Test
    void nonPreferenceEventNeverResolvesPromptEvidence() {
        UsageEventCreateRequest request = new UsageEventCreateRequest();
        request.setEventType("login");

        service.createEvent(7L, request);

        verify(preferenceObservationService, never()).resolvePromptRenderEvidence(any());
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
