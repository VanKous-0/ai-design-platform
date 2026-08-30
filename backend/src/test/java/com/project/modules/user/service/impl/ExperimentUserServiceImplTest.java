package com.project.modules.user.service.impl;

import com.project.modules.user.dto.ExperimentUserBatchCreateRequest;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import com.project.modules.user.vo.ExperimentUserCredentialVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ExperimentUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExperimentUserServiceImpl(sysUserMapper, passwordEncoder);
    }

    @Test
    void createsSequentialAnonymousAccounts() {
        ExperimentUserBatchCreateRequest request = new ExperimentUserBatchCreateRequest();
        request.setExperimentBatch("pilot-1");
        request.setExperimentGroup("A");
        request.setUsernamePrefix("user");
        request.setExperimentCodePrefix("P");
        request.setStartNumber(3);
        request.setCount(2);
        request.setInitialPassword("initial-pass");

        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("initial-pass")).thenReturn("{bcrypt}encoded");
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId("user003".equals(user.getUsername()) ? 3L : 4L);
            return 1;
        }).when(sysUserMapper).insert(any(SysUser.class));

        List<ExperimentUserCredentialVO> result = service.createBatch(request);

        assertEquals(2, result.size());
        assertEquals("user003", result.get(0).getUsername());
        assertEquals("P003", result.get(0).getExperimentCode());
        assertEquals("user004", result.get(1).getUsername());
        assertEquals("pilot-1", result.get(1).getExperimentBatch());
    }
}
