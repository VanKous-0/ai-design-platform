package com.project.modules.profile.service.impl;

import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.entity.UserPreferenceSignal;
import com.project.modules.profile.mapper.UserPreferenceSignalMapper;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceSignalServiceImplTest {

    @Mock
    private UserPreferenceSignalMapper signalMapper;

    private UserPreferenceSignalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserPreferenceSignalServiceImpl(signalMapper);
    }

    @Test
    void userDeclarationHasAuthoritativeConfidence() {
        when(signalMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            UserPreferenceSignal signal = invocation.getArgument(0);
            signal.setId(1L);
            return 1;
        }).when(signalMapper).insert(any(UserPreferenceSignal.class));

        UserPreferenceSignalVO result = service.upsert(7L, request(
                "style", "modern minimal", "USER_DECLARED", new BigDecimal("0.2")
        ));

        assertEquals("USER_DECLARED", result.getSource());
        assertEquals(new BigDecimal("1.000"), result.getConfidence());
        assertEquals(1, result.getEvidenceCount());
    }

    @Test
    void repeatedBehaviorRaisesConfidenceGradually() {
        UserPreferenceSignal existing = signal("style", "new chinese", "BEHAVIOR_INFERRED", "0.300");
        when(signalMapper.selectOne(any())).thenReturn(existing);

        UserPreferenceSignalVO result = service.upsert(7L, request(
                "style", "new chinese", "BEHAVIOR_INFERRED", new BigDecimal("1.0")
        ));

        assertEquals(new BigDecimal("0.400"), result.getConfidence());
        assertEquals(2, result.getEvidenceCount());
    }

    @Test
    void inferredSignalDoesNotOverrideUserDeclaration() {
        UserPreferenceSignal declared = signal("style", "modern minimal", "USER_DECLARED", "1.000");
        UserPreferenceSignal inferred = signal("style", "industrial", "BEHAVIOR_INFERRED", "0.800");
        when(signalMapper.selectList(any())).thenReturn(List.of(inferred, declared));

        List<UserPreferenceSignalVO> effective = service.listEffective(7L);

        assertEquals(1, effective.size());
        assertEquals("modern minimal", effective.getFirst().getPreferenceValue());
        assertEquals("USER_DECLARED", effective.getFirst().getSource());
    }

    @Test
    void agentInferenceCanBeUpdatedWithTraceableSource() {
        UserPreferenceSignal existing = signal("complexity", "rich", "AGENT_INFERRED", "0.500");
        when(signalMapper.selectOne(any())).thenReturn(existing);
        UserPreferenceSignalUpsertRequest request = request(
                "complexity", "restrained", "AGENT_INFERRED", new BigDecimal("0.700")
        );
        request.setEvidenceSummary("User said future designs should be less complex");

        UserPreferenceSignalVO result = service.upsert(7L, request);

        assertEquals("restrained", result.getPreferenceValue());
        assertEquals("AGENT_INFERRED", result.getSource());
        assertEquals(new BigDecimal("0.700"), result.getConfidence());
        assertEquals(2, result.getEvidenceCount());
        assertEquals("User said future designs should be less complex", result.getEvidenceSummary());
    }

    private UserPreferenceSignalUpsertRequest request(
            String key,
            String value,
            String source,
            BigDecimal confidence
    ) {
        UserPreferenceSignalUpsertRequest request = new UserPreferenceSignalUpsertRequest();
        request.setPreferenceKey(key);
        request.setPreferenceValue(value);
        request.setScope("LONG_TERM");
        request.setSource(source);
        request.setConfidence(confidence);
        return request;
    }

    private UserPreferenceSignal signal(String key, String value, String source, String confidence) {
        LocalDateTime now = LocalDateTime.now();
        UserPreferenceSignal signal = new UserPreferenceSignal();
        signal.setId(1L);
        signal.setUserId(7L);
        signal.setPreferenceKey(key);
        signal.setPreferenceValue(value);
        signal.setSentiment("PREFER");
        signal.setScope("LONG_TERM");
        signal.setSource(source);
        signal.setConfidence(new BigDecimal(confidence));
        signal.setEvidenceCount(1);
        signal.setLastObservedAt(now);
        signal.setCreateTime(now);
        signal.setUpdateTime(now);
        signal.setIsDeleted(0);
        return signal;
    }
}
