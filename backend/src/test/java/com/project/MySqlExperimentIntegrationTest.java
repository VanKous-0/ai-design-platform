package com.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "jwt.secret=test-secret-that-is-longer-than-thirty-two-characters",
        "app.bootstrap-admin.username=",
        "app.bootstrap-admin.password="
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MySqlExperimentIntegrationTest {

    private static final String[] SQL_FILES = {
            "schema.sql",
            "init_data.sql",
            "workflow_schema.sql",
            "workflow_init_data.sql",
            "ai_tool_schema.sql",
            "ai_tool_init_data.sql",
            "prompt_schema.sql",
            "prompt_init_data.sql",
            "case_schema.sql",
            "case_init_data.sql",
            "review_schema.sql",
            "review_init_data.sql",
            "site_schema.sql",
            "site_init_data.sql",
            "workflow_runtime_schema.sql",
            "workflow_runtime_init_data.sql",
            "prompt_enhance_schema.sql",
            "prompt_enhance_init_data.sql",
            "user_profile_schema.sql",
            "user_profile_init_data.sql",
            "rating_schema.sql",
            "rating_init_data.sql",
            "statistics_schema.sql",
            "statistics_init_data.sql",
            "case_audit_schema.sql",
            "case_audit_init_data.sql"
    };

    @org.testcontainers.junit.jupiter.Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("ai_design_platform")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("sql").toAbsolutePath()),
                    "/project-sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V27__experiment_support.sql"
                    ).toAbsolutePath()),
                    "/V27__experiment_support.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V28__real_project_cases_prompts.sql"
                    ).toAbsolutePath()),
                    "/V28__real_project_cases_prompts.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V29__workflow_step_iterations.sql"
                    ).toAbsolutePath()),
                    "/V29__workflow_step_iterations.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V30__verified_project_data_expansion.sql"
                    ).toAbsolutePath()),
                    "/V30__verified_project_data_expansion.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V31__disable_remaining_demo_content.sql"
                    ).toAbsolutePath()),
                    "/V31__disable_remaining_demo_content.sql"
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of(
                            "src/main/resources/db/migration/V32__disable_legacy_demo_cases.sql"
                    ).toAbsolutePath()),
                    "/V32__disable_legacy_demo_cases.sql"
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeAll
    void initializeDatabase() throws Exception {
        for (String file : SQL_FILES) {
            executeSqlFile("/project-sql/" + file);
        }
        executeSqlFile("/V27__experiment_support.sql");
        executeSqlFile("/V28__real_project_cases_prompts.sql");
        executeSqlFile("/V29__workflow_step_iterations.sql");
        executeSqlFile("/V30__verified_project_data_expansion.sql");
        executeSqlFile("/V31__disable_remaining_demo_content.sql");
        executeSqlFile("/V32__disable_legacy_demo_cases.sql");
        jdbcTemplate.update("""
                INSERT INTO sys_user (
                    username, password_hash, nickname, role, status,
                    experiment_code, experiment_group, experiment_batch, is_deleted
                ) VALUES (?, ?, ?, 'USER', 1, ?, ?, ?, 0)
                """, "pilot001", passwordEncoder.encode("pilot-password"), "P001",
                "P001", "A", "pilot-1");
    }

    @Test
    void realProjectMigrationReplacesDemoContent() {
        Integer realCaseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM case_project WHERE code LIKE 'REAL_%' AND is_deleted = 0",
                Integer.class
        );
        Integer demoCaseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM case_project WHERE source_desc = '开发测试数据，非最终案例内容。'",
                Integer.class
        );
        Integer originalPromptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template WHERE code LIKE 'ORIGINAL_%' AND is_deleted = 0",
                Integer.class
        );
        Integer sourcedPromptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template WHERE code LIKE 'ORIGINAL_%' AND source_desc IS NOT NULL",
                Integer.class
        );
        Integer toolUsageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM case_tool_usage",
                Integer.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(4, realCaseCount);
        org.junit.jupiter.api.Assertions.assertEquals(0, demoCaseCount);
        org.junit.jupiter.api.Assertions.assertEquals(4, originalPromptCount);
        org.junit.jupiter.api.Assertions.assertEquals(4, sourcedPromptCount);
        org.junit.jupiter.api.Assertions.assertTrue(toolUsageCount > 0);
    }

    @Test
    void loginEventStatisticsAndCsvExportUseRealMySql() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"pilot001","password":"pilot-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.experimentCode").value("P001"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.path("data").path("accessToken").asText();

        mockMvc.perform(post("/api/usage-events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType":"render_prompt",
                                  "targetType":"prompt",
                                  "targetId":1,
                                  "pageUrl":"/workflow",
                                  "extraJson":"{\\"source\\":\\"mysql-test\\"}"
                                }
                                """))
                .andExpect(status().isOk());

        Long eventCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usage_event WHERE event_type = 'render_prompt'",
                Long.class
        );
        org.junit.jupiter.api.Assertions.assertEquals(1L, eventCount);
    }

    @Test
    void verifiedProjectDataExpansionSeparatesEvidenceFromDemoContent() {
        Integer originalPrompts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template WHERE source_type = 'ORIGINAL' AND is_deleted = 0",
                Integer.class
        );
        Integer reconstructedPrompts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template WHERE source_type = 'RECONSTRUCTED' AND is_deleted = 0",
                Integer.class
        );
        Integer verifiedReviews = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_record WHERE source_type = 'VERIFIED' AND status = 1 AND is_deleted = 0",
                Integer.class
        );
        Integer enabledDemoReviews = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_record WHERE source_type = 'DEMO' AND status = 1 AND is_deleted = 0",
                Integer.class
        );
        Integer enabledDemoPrompts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template WHERE source_type = 'DEMO' AND status = 1 AND is_deleted = 0",
                Integer.class
        );
        Integer enabledApprovedCases = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM case_project
                WHERE audit_status = 'APPROVED'
                  AND status = 1
                  AND is_deleted = 0
                """,
                Integer.class
        );
        Integer achievements = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM project_achievement WHERE status = 1 AND is_deleted = 0",
                Integer.class
        );
        Integer verifiedWorkflows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workflow_template WHERE code LIKE 'verified_%' AND status = 1 AND is_deleted = 0",
                Integer.class
        );
        Integer sourcedPrompts = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM prompt_template
                WHERE source_type IN ('ORIGINAL', 'RECONSTRUCTED')
                  AND source_file IS NOT NULL
                  AND source_page IS NOT NULL
                  AND is_deleted = 0
                """,
                Integer.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(4, originalPrompts);
        org.junit.jupiter.api.Assertions.assertEquals(12, reconstructedPrompts);
        org.junit.jupiter.api.Assertions.assertEquals(9, verifiedReviews);
        org.junit.jupiter.api.Assertions.assertEquals(0, enabledDemoReviews);
        org.junit.jupiter.api.Assertions.assertEquals(0, enabledDemoPrompts);
        org.junit.jupiter.api.Assertions.assertEquals(4, enabledApprovedCases);
        org.junit.jupiter.api.Assertions.assertEquals(10, achievements);
        org.junit.jupiter.api.Assertions.assertEquals(3, verifiedWorkflows);
        org.junit.jupiter.api.Assertions.assertEquals(16, sourcedPrompts);
    }

    private void executeSqlFile(String path) throws Exception {
        Container.ExecResult result = MYSQL.execInContainer(
                "sh",
                "-c",
                "mysql -utest -ptest ai_design_platform < " + path
        );
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Failed SQL file " + path + ": " + result.getStderr());
        }
    }
}
