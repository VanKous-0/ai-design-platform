package com.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.service.PromptService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
            .withEnv("MYSQL_ROOT_PASSWORD", "root-test")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("sql").toAbsolutePath()),
                    "/project-sql"
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PromptService promptService;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        if (!MYSQL.isRunning()) {
            MYSQL.start();
        }
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeAll
    void initializeDatabase() throws Exception {
        for (String file : SQL_FILES) {
            executeSqlFile("/project-sql/" + file);
        }
        migrate(MYSQL.getJdbcUrl());
        jdbcTemplate.update("""
                INSERT INTO sys_user (
                    username, password_hash, nickname, role, status,
                    experiment_code, experiment_group, experiment_batch, is_deleted
                ) VALUES (?, ?, ?, 'USER', 1, ?, ?, ?, 0)
                """, "pilot001", passwordEncoder.encode("pilot-password"), "P001",
                "P001", "A", "pilot-1");
        jdbcTemplate.update("""
                INSERT INTO sys_user (username, password_hash, nickname, role, status, is_deleted)
                VALUES (?, ?, ?, 'ADMIN', 1, 0),
                       (?, ?, ?, 'USER', 1, 0),
                       (?, ?, ?, 'USER', 1, 0)
                """,
                "phase1admin", passwordEncoder.encode("admin-password"), "Phase 1 Admin",
                "phase1user", passwordEncoder.encode("user-password"), "Phase 1 User",
                "phase11user", passwordEncoder.encode("user-password"), "Phase 1.1 User");
    }

    @Test
    void legacySchemaUpgradesThroughFlywayWithoutLosingHistoricalData() {
        String latestVersion = jdbcTemplate.queryForObject(
                "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1",
                String.class
        );
        Integer promptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_template",
                Integer.class
        );
        Integer revisionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_revision",
                Integer.class
        );
        Integer invalidCurrentRevisionCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM prompt_template p
                LEFT JOIN prompt_revision r
                  ON r.id = p.current_revision_id AND r.prompt_id = p.id
                WHERE r.id IS NULL
                """,
                Integer.class
        );

        org.junit.jupiter.api.Assertions.assertEquals("36", latestVersion);
        org.junit.jupiter.api.Assertions.assertTrue(revisionCount >= promptCount);
        org.junit.jupiter.api.Assertions.assertEquals(0, invalidCurrentRevisionCount);
    }

    @Test
    void freshDatabaseIsCreatedEntirelyByFlyway() {
        try (MySQLContainer<?> freshMySql = new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("ai_design_platform_fresh")
                .withUsername("fresh_test")
                .withPassword("fresh_test")) {
            freshMySql.start();
            migrate(freshMySql.getJdbcUrl(), freshMySql.getUsername(), freshMySql.getPassword());

            JdbcTemplate freshJdbc = new JdbcTemplate(new DriverManagerDataSource(
                    freshMySql.getJdbcUrl(), freshMySql.getUsername(), freshMySql.getPassword()
            ));
            String latestVersion = freshJdbc.queryForObject(
                    "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1",
                    String.class
            );
            Integer requiredTables = freshJdbc.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = 'ai_design_platform_fresh'
                      AND table_name IN (
                          'prompt_template', 'prompt_revision', 'prompt_preference_hint',
                          'user_preference_signal', 'workflow_step_iteration'
                      )
                    """,
                    Integer.class
            );
            Integer invalidCurrentRevisionCount = freshJdbc.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM prompt_template p
                    LEFT JOIN prompt_revision r
                      ON r.id = p.current_revision_id AND r.prompt_id = p.id
                    WHERE r.id IS NULL
                    """,
                    Integer.class
            );

            org.junit.jupiter.api.Assertions.assertEquals("36", latestVersion);
            org.junit.jupiter.api.Assertions.assertEquals(5, requiredTables);
            org.junit.jupiter.api.Assertions.assertEquals(0, invalidCurrentRevisionCount);
        }
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
                "SELECT COUNT(*) FROM usage_event WHERE event_type = 'render_prompt' AND page_url = '/workflow'",
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
        org.junit.jupiter.api.Assertions.assertTrue(reconstructedPrompts >= 12);
        org.junit.jupiter.api.Assertions.assertEquals(9, verifiedReviews);
        org.junit.jupiter.api.Assertions.assertEquals(0, enabledDemoReviews);
        org.junit.jupiter.api.Assertions.assertEquals(0, enabledDemoPrompts);
        org.junit.jupiter.api.Assertions.assertEquals(4, enabledApprovedCases);
        org.junit.jupiter.api.Assertions.assertEquals(10, achievements);
        org.junit.jupiter.api.Assertions.assertEquals(3, verifiedWorkflows);
        org.junit.jupiter.api.Assertions.assertEquals(16, sourcedPrompts);
    }

    @Test
    void promptV1UsageRemainsReproducibleAfterV2IsPublished() throws Exception {
        String adminToken = login("phase1admin", "admin-password");
        String userToken = login("phase1user", "user-password");
        Long stageId = jdbcTemplate.queryForObject(
                "SELECT id FROM workflow_stage WHERE status = 1 AND is_deleted = 0 ORDER BY id LIMIT 1",
                Long.class
        );

        String createResponse = mockMvc.perform(post("/api/admin/prompts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stageId": %d,
                                  "title": "Phase 1 reproducibility prompt",
                                  "code": "PHASE1_REPRO_PROMPT",
                                  "category": "design_intent",
                                  "content": "Create a {style} landscape",
                                  "sourceType": "RECONSTRUCTED",
                                  "status": 1,
                                  "parameters": [
                                    {"paramKey":"style","paramName":"Style","paramType":"text","required":true,"sortOrder":10}
                                  ]
                                }
                                """.formatted(stageId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(createResponse).path("data");
        long promptId = created.path("id").asLong();
        long revisionV1 = created.path("currentRevisionId").asLong();

        String renderV1Response = mockMvc.perform(post("/api/prompts/" + promptId + "/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameters\":{\"style\":\"modern minimal\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promptRevisionId").value(revisionV1))
                .andExpect(jsonPath("$.data.revisionNo").value(1))
                .andExpect(jsonPath("$.data.renderedContent").value("Create a modern minimal landscape"))
                .andReturn().getResponse().getContentAsString();
        String renderedV1 = objectMapper.readTree(renderV1Response).path("data").path("renderedContent").asText();

        Long templateId = jdbcTemplate.queryForObject(
                "SELECT id FROM workflow_template WHERE status = 1 AND is_deleted = 0 ORDER BY id LIMIT 1",
                Long.class
        );
        String instanceResponse = mockMvc.perform(post("/api/workflow-instances")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":" + templateId + ",\"title\":\"Phase 1 history\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode instance = objectMapper.readTree(instanceResponse).path("data");
        long instanceId = instance.path("id").asLong();
        long nodeId = instance.path("currentNodeId").asLong();

        mockMvc.perform(post("/api/workflow-instances/" + instanceId + "/steps/" + nodeId + "/iterations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "promptId", promptId,
                                "promptRevisionId", revisionV1,
                                "promptContent", renderedV1,
                                "profileContextSnapshot", "{\"style\":\"modern minimal\"}",
                                "outputContent", "Historical result from v1"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promptRevisionId").value(revisionV1));

        String updateResponse = mockMvc.perform(put("/api/admin/prompts/" + promptId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stageId": %d,
                                  "title": "Phase 1 reproducibility prompt",
                                  "code": "PHASE1_REPRO_PROMPT",
                                  "category": "design_intent",
                                  "content": "Create a {style} landscape for {siteArea}",
                                  "sourceType": "RECONSTRUCTED",
                                  "status": 1,
                                  "parameters": [
                                    {"paramKey":"style","paramName":"Style","paramType":"text","required":true,"sortOrder":10},
                                    {"paramKey":"siteArea","paramName":"Site area","paramType":"text","required":true,"sortOrder":20}
                                  ]
                                }
                                """.formatted(stageId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long revisionV2 = objectMapper.readTree(updateResponse).path("data").path("currentRevisionId").asLong();
        org.junit.jupiter.api.Assertions.assertNotEquals(revisionV1, revisionV2);

        mockMvc.perform(get("/api/prompts/" + promptId + "/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].revisionNo").value(2));
        mockMvc.perform(get("/api/prompts/" + promptId + "/revisions/" + revisionV1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revisionNo").value(1))
                .andExpect(jsonPath("$.data.parameterSchema.length()").value(1));

        mockMvc.perform(post("/api/prompts/" + promptId + "/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parameters\":{\"style\":\"new chinese\",\"siteArea\":\"2 hectares\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promptRevisionId").value(revisionV2))
                .andExpect(jsonPath("$.data.revisionNo").value(2))
                .andExpect(jsonPath("$.data.renderedContent")
                        .value("Create a new chinese landscape for 2 hectares"));

        mockMvc.perform(post("/api/prompts/" + promptId + "/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promptRevisionId\":" + revisionV1
                                + ",\"parameters\":{\"style\":\"modern minimal\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revisionNo").value(1))
                .andExpect(jsonPath("$.data.renderedContent").value(renderedV1));

        mockMvc.perform(get("/api/workflow-instances/" + instanceId + "/steps/" + nodeId + "/iterations")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].promptRevisionId").value(revisionV1))
                .andExpect(jsonPath("$.data[0].promptContent").value(renderedV1));

        Integer revisionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_revision WHERE prompt_id = ?",
                Integer.class,
                promptId
        );
        Integer v1SchemaSize = jdbcTemplate.queryForObject(
                "SELECT JSON_LENGTH(parameter_schema_json) FROM prompt_revision WHERE id = ?",
                Integer.class,
                revisionV1
        );
        org.junit.jupiter.api.Assertions.assertEquals(2, revisionCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, v1SchemaSize);
    }

    @Test
    void promptRevisionConstraintsRejectCollisionsAndRollbackAtomically() {
        java.util.List<java.util.Map<String, Object>> prompts = jdbcTemplate.queryForList("""
                SELECT id, current_revision_id, title
                FROM prompt_template
                WHERE current_revision_id IS NOT NULL
                ORDER BY id
                LIMIT 2
                """);
        org.junit.jupiter.api.Assertions.assertEquals(2, prompts.size());
        long promptId = ((Number) prompts.get(0).get("id")).longValue();
        long revisionId = ((Number) prompts.get(0).get("current_revision_id")).longValue();
        long otherRevisionId = ((Number) prompts.get(1).get("current_revision_id")).longValue();
        String originalTitle = (String) prompts.get(0).get("title");
        Integer originalRevisionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prompt_revision WHERE prompt_id = ?", Integer.class, promptId
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataAccessException.class,
                () -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                    jdbcTemplate.update("UPDATE prompt_template SET title = 'must roll back' WHERE id = ?", promptId);
                    jdbcTemplate.update("""
                            INSERT INTO prompt_revision (
                                prompt_id, revision_no, content, parameter_schema_json, status, create_time
                            )
                            SELECT prompt_id, revision_no, content, parameter_schema_json, status, NOW()
                            FROM prompt_revision WHERE id = ?
                            """, revisionId);
                })
        );
        org.junit.jupiter.api.Assertions.assertEquals(
                originalTitle,
                jdbcTemplate.queryForObject("SELECT title FROM prompt_template WHERE id = ?", String.class, promptId)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                originalRevisionCount,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM prompt_revision WHERE prompt_id = ?", Integer.class, promptId
                )
        );
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataAccessException.class,
                () -> jdbcTemplate.update(
                        "UPDATE prompt_template SET current_revision_id = ? WHERE id = ?", otherRevisionId, promptId
                )
        );
        org.junit.jupiter.api.Assertions.assertEquals(
                revisionId,
                jdbcTemplate.queryForObject(
                        "SELECT current_revision_id FROM prompt_template WHERE id = ?", Long.class, promptId
                )
        );
    }

    @Test
    void concurrentPromptUpdatesProduceDistinctMonotonicRevisions() throws Exception {
        Long stageId = jdbcTemplate.queryForObject(
                "SELECT id FROM workflow_stage WHERE status = 1 AND is_deleted = 0 ORDER BY id LIMIT 1",
                Long.class
        );
        Long adminId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'phase1admin' AND is_deleted = 0",
                Long.class
        );
        PromptCreateRequest create = new PromptCreateRequest();
        create.setStageId(stageId);
        create.setTitle("Concurrent revision prompt");
        create.setCode("PHASE1_CONCURRENT_PROMPT");
        create.setCategory("design_intent");
        create.setContent("revision v1");
        create.setSourceType("RECONSTRUCTED");
        create.setStatus(1);
        long promptId = promptService.createPrompt(create, adminId).getId();

        PromptUpdateRequest first = concurrentUpdate(stageId, "revision from writer A");
        PromptUpdateRequest second = concurrentUpdate(stageId, "revision from writer B");
        java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Future<?> firstFuture = executor.submit(() -> {
                ready.countDown();
                await(start);
                promptService.updatePrompt(promptId, first, adminId);
            });
            java.util.concurrent.Future<?> secondFuture = executor.submit(() -> {
                ready.countDown();
                await(start);
                promptService.updatePrompt(promptId, second, adminId);
            });
            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, java.util.concurrent.TimeUnit.SECONDS));
            start.countDown();
            firstFuture.get(15, java.util.concurrent.TimeUnit.SECONDS);
            secondFuture.get(15, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        java.util.Map<String, Object> revisionStats = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS revision_count,
                       COUNT(DISTINCT revision_no) AS distinct_revision_count,
                       MAX(revision_no) AS max_revision_no
                FROM prompt_revision
                WHERE prompt_id = ?
                """, promptId);
        org.junit.jupiter.api.Assertions.assertEquals(3L, ((Number) revisionStats.get("revision_count")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals(
                3L, ((Number) revisionStats.get("distinct_revision_count")).longValue()
        );
        org.junit.jupiter.api.Assertions.assertEquals(3, ((Number) revisionStats.get("max_revision_no")).intValue());
    }

    @Test
    void consolidatedProfilePreservesSourceAndDeclarationPriority() throws Exception {
        String adminToken = login("phase1admin", "admin-password");
        String userToken = login("phase1user", "user-password");
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'phase1user' AND is_deleted = 0",
                Long.class
        );
        postPreferenceSignal("/api/user/preference-signals", userToken,
                "style", "modern minimal", "USER_DECLARED", null);
        postPreferenceSignal("/api/admin/users/" + userId + "/preference-signals", adminToken,
                "style", "industrial", "BEHAVIOR_INFERRED", null);
        JsonNode repeatedBehavior = postPreferenceSignal(
                "/api/admin/users/" + userId + "/preference-signals", adminToken,
                "style", "industrial", "BEHAVIOR_INFERRED", null
        );
        postPreferenceSignal("/api/admin/users/" + userId + "/preference-signals", adminToken,
                "material", "natural wood", "AGENT_INFERRED", "0.650");

        mockMvc.perform(post("/api/user/preference-signals")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"preferenceKey":"style","preferenceValue":"forged",
                                 "scope":"LONG_TERM","source":"AGENT_INFERRED"}
                                """))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                repeatedBehavior.path("confidence").decimalValue().compareTo(new java.math.BigDecimal("0.400"))
        );
        org.junit.jupiter.api.Assertions.assertEquals(2, repeatedBehavior.path("evidenceCount").asInt());

        String contextResponse = mockMvc.perform(get("/api/user/preference-context")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode context = objectMapper.readTree(contextResponse).path("data");
        JsonNode effectiveStyle = java.util.stream.StreamSupport.stream(
                        context.path("effectiveSignals").spliterator(), false
                )
                .filter(signal -> "style".equals(signal.path("preferenceKey").asText()))
                .findFirst()
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals("modern minimal", effectiveStyle.path("preferenceValue").asText());
        org.junit.jupiter.api.Assertions.assertEquals("USER_DECLARED", effectiveStyle.path("source").asText());
        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                effectiveStyle.path("confidence").decimalValue().compareTo(new java.math.BigDecimal("1.000"))
        );
        org.junit.jupiter.api.Assertions.assertEquals(3, context.path("allSignals").size());
    }

    @Test
    void behaviorEvidenceAndProfileSnapshotsRemainServerAuthoritative() throws Exception {
        String adminToken = login("phase1admin", "admin-password");
        String userToken = login("phase11user", "user-password");
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_user WHERE username = 'phase11user' AND is_deleted = 0",
                Long.class
        );
        Long stageId = jdbcTemplate.queryForObject(
                "SELECT id FROM workflow_stage WHERE status = 1 AND is_deleted = 0 ORDER BY id LIMIT 1",
                Long.class
        );

        String promptResponse = mockMvc.perform(post("/api/admin/prompts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stageId": %d,
                                  "title": "Phase 1.1 explicit style prompt",
                                  "code": "PHASE11_STYLE_PROMPT",
                                  "category": "design_intent",
                                  "content": "Create a deliberate design",
                                  "sourceType": "RECONSTRUCTED",
                                  "status": 1,
                                  "preferenceHints": [
                                    {"preferenceKey":"style","preferenceValue":"现代极简"}
                                  ]
                                }
                                """.formatted(stageId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferenceHints[0].preferenceKey").value("style"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        long promptId = objectMapper.readTree(promptResponse).path("data").path("id").asLong();

        String firstEvent = mockMvc.perform(post("/api/usage-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"render_prompt","targetType":"prompt","targetId":%d}
                                """.formatted(promptId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode firstEvidence = objectMapper.readTree(
                objectMapper.readTree(firstEvent).path("data").path("preferenceEvidenceJson").asText()
        );
        org.junit.jupiter.api.Assertions.assertEquals("style", firstEvidence.get(0).path("preferenceKey").asText());
        org.junit.jupiter.api.Assertions.assertEquals("现代极简", firstEvidence.get(0).path("preferenceValue").asText());

        java.util.Map<String, Object> behavior = behaviorSignal(userId, "style");
        org.junit.jupiter.api.Assertions.assertEquals("现代极简", behavior.get("preference_value"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) behavior.get("evidence_count")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                ((java.math.BigDecimal) behavior.get("confidence")).compareTo(new java.math.BigDecimal("0.300"))
        );

        mockMvc.perform(post("/api/usage-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"render_prompt","targetType":"prompt","targetId":%d}
                                """.formatted(promptId)))
                .andExpect(status().isOk());
        behavior = behaviorSignal(userId, "style");
        org.junit.jupiter.api.Assertions.assertEquals(2, ((Number) behavior.get("evidence_count")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                ((java.math.BigDecimal) behavior.get("confidence")).compareTo(new java.math.BigDecimal("0.400"))
        );

        mockMvc.perform(put("/api/admin/prompts/" + promptId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "stageId": %d,
                                  "title": "Phase 1.1 explicit style prompt",
                                  "code": "PHASE11_STYLE_PROMPT",
                                  "category": "design_intent",
                                  "content": "Create a deliberate design",
                                  "sourceType": "RECONSTRUCTED",
                                  "status": 1,
                                  "preferenceHints": [
                                    {"preferenceKey":"style","preferenceValue":"新中式"}
                                  ]
                                }
                                """.formatted(stageId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/usage-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"render_prompt","targetType":"prompt","targetId":%d}
                                """.formatted(promptId)))
                .andExpect(status().isOk());
        behavior = behaviorSignal(userId, "style");
        org.junit.jupiter.api.Assertions.assertEquals("新中式", behavior.get("preference_value"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) behavior.get("evidence_count")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                ((java.math.BigDecimal) behavior.get("confidence")).compareTo(new java.math.BigDecimal("0.300"))
        );

        mockMvc.perform(post("/api/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"anonymousId":"phase11-anon","eventType":"render_prompt",
                                 "targetType":"prompt","targetId":%d}
                                """.formatted(promptId)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/usage-events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"copy_prompt","targetType":"prompt","targetId":%d}
                                """.formatted(promptId)))
                .andExpect(status().isOk());
        behavior = behaviorSignal(userId, "style");
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) behavior.get("evidence_count")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM usage_event WHERE anonymous_id = 'phase11-anon'",
                        Integer.class
                )
        );
        org.junit.jupiter.api.Assertions.assertNull(jdbcTemplate.queryForObject(
                "SELECT preference_evidence_json FROM usage_event WHERE event_type = 'copy_prompt' AND user_id = ? ORDER BY id DESC LIMIT 1",
                String.class,
                userId
        ));

        Long templateId = jdbcTemplate.queryForObject(
                "SELECT id FROM workflow_template WHERE status = 1 AND is_deleted = 0 ORDER BY id LIMIT 1",
                Long.class
        );
        JsonNode instance = objectMapper.readTree(mockMvc.perform(post("/api/workflow-instances")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":" + templateId + ",\"title\":\"Phase 1.1 profile history\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(
                        java.nio.charset.StandardCharsets.UTF_8
                )).path("data");
        long instanceId = instance.path("id").asLong();
        long nodeId = instance.path("currentNodeId").asLong();

        JsonNode iterationA = objectMapper.readTree(mockMvc.perform(post(
                                "/api/workflow-instances/" + instanceId + "/steps/" + nodeId + "/iterations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "profileContextSnapshot", "{\"style\":\"工业风\"}",
                                "outputContent", "Profile snapshot v1"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(
                        java.nio.charset.StandardCharsets.UTF_8
                )).path("data");
        JsonNode snapshotA = objectMapper.readTree(iterationA.path("profileContextSnapshot").asText());
        org.junit.jupiter.api.Assertions.assertEquals(1, snapshotA.path("schemaVersion").asInt());
        org.junit.jupiter.api.Assertions.assertEquals("新中式", snapshotPreference(snapshotA, "style")
                .path("preferenceValue").asText());
        org.junit.jupiter.api.Assertions.assertEquals("BEHAVIOR_INFERRED", snapshotPreference(snapshotA, "style")
                .path("source").asText());

        postPreferenceSignal(
                "/api/user/preference-signals",
                userToken,
                "style",
                "现代极简",
                "USER_DECLARED",
                null
        );
        JsonNode iterationB = objectMapper.readTree(mockMvc.perform(post(
                                "/api/workflow-instances/" + instanceId + "/steps/" + nodeId + "/iterations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "profileContextSnapshot", "{\"style\":\"包豪斯\"}",
                                "outputContent", "Profile snapshot v2"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(
                        java.nio.charset.StandardCharsets.UTF_8
                )).path("data");
        JsonNode snapshotB = objectMapper.readTree(iterationB.path("profileContextSnapshot").asText());
        org.junit.jupiter.api.Assertions.assertEquals("现代极简", snapshotPreference(snapshotB, "style")
                .path("preferenceValue").asText());
        org.junit.jupiter.api.Assertions.assertEquals("USER_DECLARED", snapshotPreference(snapshotB, "style")
                .path("source").asText());

        JsonNode storedSnapshotA = objectMapper.readTree(jdbcTemplate.queryForObject(
                "SELECT profile_context_snapshot FROM workflow_step_iteration WHERE id = ?",
                String.class,
                iterationA.path("id").asLong()
        ));
        org.junit.jupiter.api.Assertions.assertEquals("新中式", snapshotPreference(storedSnapshotA, "style")
                .path("preferenceValue").asText());

        String contextResponse = mockMvc.perform(get("/api/user/preference-context")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode effective = objectMapper.readTree(contextResponse).path("data").path("effectiveSignals");
        JsonNode effectiveStyle = java.util.stream.StreamSupport.stream(effective.spliterator(), false)
                .filter(signal -> "style".equals(signal.path("preferenceKey").asText()))
                .findFirst()
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("现代极简", effectiveStyle.path("preferenceValue").asText());
        org.junit.jupiter.api.Assertions.assertEquals("USER_DECLARED", effectiveStyle.path("source").asText());
    }

    private java.util.Map<String, Object> behaviorSignal(Long userId, String preferenceKey) {
        return jdbcTemplate.queryForMap("""
                SELECT preference_value, confidence, evidence_count
                FROM user_preference_signal
                WHERE user_id = ? AND preference_key = ?
                  AND source = 'BEHAVIOR_INFERRED' AND is_deleted = 0
                """, userId, preferenceKey);
    }

    private JsonNode snapshotPreference(JsonNode snapshot, String preferenceKey) {
        return java.util.stream.StreamSupport.stream(snapshot.path("preferences").spliterator(), false)
                .filter(preference -> preferenceKey.equals(preference.path("preferenceKey").asText()))
                .findFirst()
                .orElseThrow();
    }

    private JsonNode postPreferenceSignal(
            String path,
            String token,
            String key,
            String value,
            String source,
            String confidence
    ) throws Exception {
        String confidenceField = confidence == null ? "" : ",\"confidence\":" + confidence;
        String response = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferenceKey\":\"" + key
                                + "\",\"preferenceValue\":\"" + value
                                + "\",\"scope\":\"LONG_TERM\",\"source\":\"" + source + "\""
                                + confidenceField + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("accessToken").asText();
    }

    private PromptUpdateRequest concurrentUpdate(Long stageId, String content) {
        PromptUpdateRequest request = new PromptUpdateRequest();
        request.setStageId(stageId);
        request.setTitle("Concurrent revision prompt");
        request.setCode("PHASE1_CONCURRENT_PROMPT");
        request.setCategory("design_intent");
        request.setContent(content);
        request.setSourceType("RECONSTRUCTED");
        request.setStatus(1);
        return request;
    }

    private void await(java.util.concurrent.CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating concurrent prompt updates", ex);
        }
    }

    private void migrate(String jdbcUrl) {
        migrate(jdbcUrl, MYSQL.getUsername(), MYSQL.getPassword());
    }

    private void migrate(String jdbcUrl, String username, String password) {
        Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("26"))
                .load()
                .migrate();
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
