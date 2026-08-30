package com.project;

import com.project.common.util.JwtUtil;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private SysUserMapper sysUserMapper;

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void caseAssetsArePubliclyReadable() throws Exception {
        mockMvc.perform(get("/assets/cases/visitor-center/cover.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void invalidJwtReturnsStructuredUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void normalUserCannotAccessAdminStatistics() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setRole("USER");
        user.setStatus(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        String token = jwtUtil.generateToken(1L, "testuser", "USER");

        mockMvc.perform(get("/api/admin/statistics/workflow-summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void disabledUserTokenIsRejectedImmediately() throws Exception {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setRole("USER");
        user.setStatus(0);
        when(sysUserMapper.selectById(2L)).thenReturn(user);
        String token = jwtUtil.generateToken(2L, "disabled-user", "USER");

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void oversizedAnonymousFeedbackIsRejectedBeforeDatabaseAccess() throws Exception {
        String oversizedScene = "x".repeat(101);

        mockMvc.perform(post("/api/survey-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scene": "%s",
                                  "score": 8.0
                                }
                                """.formatted(oversizedScene)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
