package com.project.modules.statistics.service.impl;

import com.project.common.exception.BusinessException;
import com.project.modules.statistics.service.ExperimentExportService;
import com.project.modules.statistics.support.CsvWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class ExperimentExportServiceImpl implements ExperimentExportService {

    private static final List<String> USER_HEADERS = List.of(
            "user_id", "username", "experiment_code", "experiment_group", "experiment_batch", "status", "create_time"
    );
    private static final List<String> EVENT_HEADERS = List.of(
            "event_id", "user_id", "experiment_code", "experiment_group", "experiment_batch", "anonymous_id",
            "event_type", "target_type", "target_id", "page_url", "stay_duration", "input_summary",
            "extra_json", "preference_evidence_json", "event_time"
    );
    private static final List<String> WORKFLOW_HEADERS = List.of(
            "record_id", "instance_id", "user_id", "experiment_code", "experiment_group", "experiment_batch",
            "template_id", "template_name", "instance_title", "instance_status", "instance_progress",
            "node_id", "node_name", "step_status", "duration_seconds", "started_at", "completed_at",
            "instance_start_time", "instance_finish_time"
    );
    private static final List<String> ITERATION_HEADERS = List.of(
            "iteration_id", "instance_id", "user_id", "experiment_code", "experiment_group", "experiment_batch",
            "template_id", "template_name", "node_id", "node_name", "iteration_no", "tool_id", "tool_name",
            "prompt_id", "prompt_revision_id", "prompt_content", "profile_context_snapshot",
            "output_content", "result_url", "effect_score", "accuracy_score",
            "controllability_score", "usability_score", "average_score", "improvement_note", "selected",
            "create_time", "update_time"
    );
    private static final List<String> RATING_HEADERS = List.of(
            "rating_type", "rating_id", "user_id", "experiment_code", "experiment_group", "experiment_batch",
            "target_id", "target_name", "instance_id", "effect_score", "ease_score", "stability_score",
            "recommend_score", "comment", "create_time", "update_time"
    );
    private static final List<String> SURVEY_HEADERS = List.of(
            "feedback_id", "user_id", "experiment_code", "experiment_group", "experiment_batch", "anonymous_id",
            "scene", "score", "content", "contact", "create_time"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExperimentExportServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public byte[] exportUsers(String experimentBatch, String experimentGroup) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, null, null);
        String sql = """
                SELECT
                    u.id AS user_id,
                    u.username,
                    u.experiment_code,
                    u.experiment_group,
                    u.experiment_batch,
                    u.status,
                    u.create_time
                FROM sys_user u
                WHERE u.is_deleted = 0
                  AND u.role = 'USER'
                  AND u.experiment_code IS NOT NULL
                """ + userFilters("u", params, false) + """
                ORDER BY u.id
                """;
        return CsvWriter.write(USER_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    @Override
    public byte[] exportUsageEvents(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, startDate, endDate);
        String sql = """
                SELECT
                    e.id AS event_id,
                    e.user_id,
                    u.experiment_code,
                    u.experiment_group,
                    u.experiment_batch,
                    e.anonymous_id,
                    e.event_type,
                    e.target_type,
                    e.target_id,
                    e.page_url,
                    e.stay_duration,
                    e.input_summary,
                    e.extra_json,
                    e.preference_evidence_json,
                    e.create_time AS event_time
                FROM usage_event e
                LEFT JOIN sys_user u ON u.id = e.user_id AND u.is_deleted = 0
                WHERE e.is_deleted = 0
                """ + userFilters("u", params, true) + dateFilters("e.create_time", params) + """
                ORDER BY e.create_time, e.id
                """;
        return CsvWriter.write(EVENT_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    @Override
    public byte[] exportWorkflowRecords(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, startDate, endDate);
        String sql = """
                SELECT
                    sr.id AS record_id,
                    wi.id AS instance_id,
                    wi.user_id,
                    u.experiment_code,
                    u.experiment_group,
                    u.experiment_batch,
                    wi.template_id,
                    wt.name AS template_name,
                    wi.title AS instance_title,
                    wi.status AS instance_status,
                    wi.progress AS instance_progress,
                    sr.node_id,
                    wn.node_name,
                    sr.status AS step_status,
                    sr.duration_seconds,
                    sr.started_at,
                    sr.completed_at,
                    wi.start_time AS instance_start_time,
                    wi.finish_time AS instance_finish_time
                FROM workflow_instance wi
                INNER JOIN sys_user u ON u.id = wi.user_id AND u.is_deleted = 0
                INNER JOIN workflow_template wt ON wt.id = wi.template_id AND wt.is_deleted = 0
                LEFT JOIN workflow_step_record sr ON sr.instance_id = wi.id AND sr.is_deleted = 0
                LEFT JOIN workflow_template_node wn ON wn.id = sr.node_id AND wn.is_deleted = 0
                WHERE wi.is_deleted = 0
                """ + userFilters("u", params, false) + dateFilters("wi.create_time", params) + """
                ORDER BY wi.create_time, wi.id, sr.id
                """;
        return CsvWriter.write(WORKFLOW_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    @Override
    public byte[] exportWorkflowIterations(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, startDate, endDate);
        String sql = """
                SELECT
                    i.id AS iteration_id,
                    i.instance_id,
                    i.user_id,
                    u.experiment_code,
                    u.experiment_group,
                    u.experiment_batch,
                    wi.template_id,
                    wt.name AS template_name,
                    i.node_id,
                    wn.node_name,
                    i.iteration_no,
                    i.tool_id,
                    t.name AS tool_name,
                    i.prompt_id,
                    i.prompt_revision_id,
                    i.prompt_content,
                    i.profile_context_snapshot,
                    i.output_content,
                    i.result_url,
                    i.effect_score,
                    i.accuracy_score,
                    i.controllability_score,
                    i.usability_score,
                    ROUND((
                        COALESCE(i.effect_score, 0)
                        + COALESCE(i.accuracy_score, 0)
                        + COALESCE(i.controllability_score, 0)
                        + COALESCE(i.usability_score, 0)
                    ) / NULLIF(
                        (i.effect_score IS NOT NULL)
                        + (i.accuracy_score IS NOT NULL)
                        + (i.controllability_score IS NOT NULL)
                        + (i.usability_score IS NOT NULL),
                        0
                    ), 2) AS average_score,
                    i.improvement_note,
                    i.selected,
                    i.create_time,
                    i.update_time
                FROM workflow_step_iteration i
                INNER JOIN workflow_instance wi ON wi.id = i.instance_id AND wi.is_deleted = 0
                INNER JOIN workflow_template wt ON wt.id = wi.template_id AND wt.is_deleted = 0
                INNER JOIN workflow_template_node wn ON wn.id = i.node_id AND wn.is_deleted = 0
                INNER JOIN sys_user u ON u.id = i.user_id AND u.is_deleted = 0
                LEFT JOIN ai_tool t ON t.id = i.tool_id AND t.is_deleted = 0
                WHERE i.is_deleted = 0
                """ + userFilters("u", params, false) + dateFilters("i.create_time", params) + """
                ORDER BY i.create_time, i.instance_id, i.node_id, i.iteration_no
                """;
        return CsvWriter.write(ITERATION_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    @Override
    public byte[] exportRatings(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, startDate, endDate);
        String toolFilters = userFilters("u", params, false) + dateFilters("r.create_time", params);
        String workflowFilters = userFilters("u", params, false) + dateFilters("r.create_time", params);
        String sql = """
                SELECT * FROM (
                    SELECT
                        'TOOL' AS rating_type,
                        r.id AS rating_id,
                        r.user_id,
                        u.experiment_code,
                        u.experiment_group,
                        u.experiment_batch,
                        r.tool_id AS target_id,
                        t.name AS target_name,
                        NULL AS instance_id,
                        r.effect_score,
                        r.ease_score,
                        r.stability_score,
                        r.recommend_score,
                        r.comment,
                        r.create_time,
                        r.update_time
                    FROM user_tool_rating r
                    INNER JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0
                    INNER JOIN ai_tool t ON t.id = r.tool_id AND t.is_deleted = 0
                    WHERE r.is_deleted = 0
                """ + toolFilters + """
                    UNION ALL
                    SELECT
                        'WORKFLOW' AS rating_type,
                        r.id AS rating_id,
                        r.user_id,
                        u.experiment_code,
                        u.experiment_group,
                        u.experiment_batch,
                        r.template_id AS target_id,
                        t.name AS target_name,
                        r.instance_id,
                        r.effect_score,
                        r.ease_score,
                        r.stability_score,
                        r.recommend_score,
                        r.comment,
                        r.create_time,
                        r.update_time
                    FROM user_workflow_rating r
                    INNER JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0
                    INNER JOIN workflow_template t ON t.id = r.template_id AND t.is_deleted = 0
                    WHERE r.is_deleted = 0
                """ + workflowFilters + """
                ) ratings
                ORDER BY create_time, rating_type, rating_id
                """;
        return CsvWriter.write(RATING_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    @Override
    public byte[] exportSurveyFeedback(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        MapSqlParameterSource params = baseParams(experimentBatch, experimentGroup, startDate, endDate);
        String sql = """
                SELECT
                    f.id AS feedback_id,
                    f.user_id,
                    u.experiment_code,
                    u.experiment_group,
                    u.experiment_batch,
                    f.anonymous_id,
                    f.scene,
                    f.score,
                    f.content,
                    f.contact,
                    f.create_time
                FROM survey_feedback f
                LEFT JOIN sys_user u ON u.id = f.user_id AND u.is_deleted = 0
                WHERE f.is_deleted = 0
                """ + userFilters("u", params, true) + dateFilters("f.create_time", params) + """
                ORDER BY f.create_time, f.id
                """;
        return CsvWriter.write(SURVEY_HEADERS, jdbcTemplate.queryForList(sql, params));
    }

    private MapSqlParameterSource baseParams(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("startDate不能晚于endDate");
        }
        return new MapSqlParameterSource()
                .addValue("experimentBatch", trimToNull(experimentBatch))
                .addValue("experimentGroup", trimToNull(experimentGroup))
                .addValue("startTime", startDate == null ? null : startDate.atStartOfDay())
                .addValue("endTime", endDate == null ? null : endDate.atTime(LocalTime.MAX));
    }

    private String userFilters(String alias, MapSqlParameterSource params, boolean allowAnonymous) {
        StringBuilder sql = new StringBuilder();
        if (params.getValue("experimentBatch") != null) {
            sql.append(" AND ").append(alias).append(".experiment_batch = :experimentBatch\n");
        }
        if (params.getValue("experimentGroup") != null) {
            sql.append(" AND ").append(alias).append(".experiment_group = :experimentGroup\n");
        }
        if (!allowAnonymous) {
            sql.append(" AND ").append(alias).append(".experiment_code IS NOT NULL\n");
        }
        return sql.toString();
    }

    private String dateFilters(String column, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder();
        if (params.getValue("startTime") != null) {
            sql.append(" AND ").append(column).append(" >= :startTime\n");
        }
        if (params.getValue("endTime") != null) {
            sql.append(" AND ").append(column).append(" <= :endTime\n");
        }
        return sql.toString();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
