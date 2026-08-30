package com.project.modules.statistics.mapper;

import com.project.modules.statistics.vo.EventTypeSummaryVO;
import com.project.modules.statistics.vo.PromptUsageSummaryVO;
import com.project.modules.statistics.vo.TargetTypeSummaryVO;
import com.project.modules.statistics.vo.ToolRatingStatisticsVO;
import com.project.modules.statistics.vo.UsageSummaryVO;
import com.project.modules.statistics.vo.WorkflowRatingStatisticsVO;
import com.project.modules.statistics.vo.WorkflowStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StatisticsQueryMapper {

    @Select("""
            <script>
            SELECT
                COUNT(*) AS total_event_count,
                SUM(CASE WHEN user_id IS NOT NULL THEN 1 ELSE 0 END) AS login_user_event_count,
                SUM(CASE WHEN user_id IS NULL THEN 1 ELSE 0 END) AS anonymous_event_count,
                COUNT(DISTINCT user_id) AS unique_user_count,
                COUNT(DISTINCT CASE
                    WHEN anonymous_id IS NOT NULL AND TRIM(anonymous_id) != '' THEN anonymous_id
                END) AS unique_anonymous_count,
                COALESCE(SUM(stay_duration), 0) AS total_stay_duration,
                COALESCE(ROUND(AVG(stay_duration), 1), 0.0) AS average_stay_duration
            FROM usage_event
            WHERE is_deleted = 0
            <if test="startTime != null">
                AND create_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
                AND create_time &lt;= #{endTime}
            </if>
            </script>
            """)
    UsageSummaryVO selectUsageSummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            <script>
            SELECT event_type, COUNT(*) AS count
            FROM usage_event
            WHERE is_deleted = 0
              AND event_type IS NOT NULL
              AND TRIM(event_type) != ''
            <if test="startTime != null">
                AND create_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
                AND create_time &lt;= #{endTime}
            </if>
            GROUP BY event_type
            ORDER BY count DESC, event_type ASC
            </script>
            """)
    List<EventTypeSummaryVO> selectEventTypeSummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            <script>
            SELECT target_type, COUNT(*) AS count
            FROM usage_event
            WHERE is_deleted = 0
              AND target_type IS NOT NULL
              AND TRIM(target_type) != ''
            <if test="startTime != null">
                AND create_time &gt;= #{startTime}
            </if>
            <if test="endTime != null">
                AND create_time &lt;= #{endTime}
            </if>
            GROUP BY target_type
            ORDER BY count DESC, target_type ASC
            </script>
            """)
    List<TargetTypeSummaryVO> selectTargetTypeSummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            <script>
            SELECT
                p.id AS prompt_id,
                p.title AS prompt_title,
                p.copy_count,
                COALESCE(e.render_count, 0) AS render_count,
                COALESCE(e.event_count, 0) AS event_count
            FROM prompt_template p
            LEFT JOIN (
                SELECT
                    target_id,
                    SUM(CASE WHEN event_type = 'render_prompt' THEN 1 ELSE 0 END) AS render_count,
                    COUNT(*) AS event_count
                FROM usage_event
                WHERE is_deleted = 0
                  AND target_type = 'prompt'
                  AND target_id IS NOT NULL
                <if test="startTime != null">
                    AND create_time &gt;= #{startTime}
                </if>
                <if test="endTime != null">
                    AND create_time &lt;= #{endTime}
                </if>
                GROUP BY target_id
            ) e ON e.target_id = p.id
            WHERE p.is_deleted = 0
            ORDER BY p.sort_order ASC, p.id ASC
            </script>
            """)
    List<PromptUsageSummaryVO> selectPromptSummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            SELECT
                t.id AS template_id,
                t.name AS template_name,
                COALESCE(i.instance_count, 0) AS instance_count,
                COALESCE(i.running_count, 0) AS running_count,
                COALESCE(i.finished_count, 0) AS finished_count,
                COALESCE(i.average_progress, 0.0) AS average_progress,
                COALESCE(s.complete_step_count, 0) AS complete_step_count
            FROM workflow_template t
            LEFT JOIN (
                SELECT
                    template_id,
                    COUNT(*) AS instance_count,
                    SUM(CASE WHEN status = 'RUNNING' THEN 1 ELSE 0 END) AS running_count,
                    SUM(CASE WHEN status = 'FINISHED' THEN 1 ELSE 0 END) AS finished_count,
                    ROUND(AVG(progress), 1) AS average_progress
                FROM workflow_instance
                WHERE is_deleted = 0
                GROUP BY template_id
            ) i ON i.template_id = t.id
            LEFT JOIN (
                SELECT
                    wi.template_id,
                    COUNT(*) AS complete_step_count
                FROM workflow_step_record sr
                INNER JOIN workflow_instance wi
                    ON wi.id = sr.instance_id
                   AND wi.is_deleted = 0
                WHERE sr.is_deleted = 0
                  AND sr.status = 'COMPLETED'
                GROUP BY wi.template_id
            ) s ON s.template_id = t.id
            WHERE t.is_deleted = 0
            ORDER BY t.sort_order ASC, t.id ASC
            """)
    List<WorkflowStatisticsVO> selectWorkflowSummary();

    @Select("""
            SELECT
                t.id AS tool_id,
                t.name AS tool_name,
                COUNT(r.id) AS rating_count,
                COALESCE(ROUND(AVG(r.effect_score), 1), 0.0) AS average_effect_score,
                COALESCE(ROUND(AVG(r.ease_score), 1), 0.0) AS average_ease_score,
                COALESCE(ROUND(AVG(r.stability_score), 1), 0.0) AS average_stability_score,
                COALESCE(ROUND(AVG(r.recommend_score), 1), 0.0) AS average_recommend_score,
                COALESCE(ROUND(AVG(
                    (r.effect_score + r.ease_score + r.stability_score + r.recommend_score) / 4
                ), 1), 0.0) AS average_total_score
            FROM ai_tool t
            LEFT JOIN user_tool_rating r
                ON r.tool_id = t.id
               AND r.is_deleted = 0
            WHERE t.is_deleted = 0
            GROUP BY t.id, t.name
            ORDER BY rating_count DESC, t.id ASC
            """)
    List<ToolRatingStatisticsVO> selectToolRatingSummary();

    @Select("""
            SELECT
                t.id AS template_id,
                t.name AS template_name,
                COUNT(r.id) AS rating_count,
                COALESCE(ROUND(AVG(r.effect_score), 1), 0.0) AS average_effect_score,
                COALESCE(ROUND(AVG(r.ease_score), 1), 0.0) AS average_ease_score,
                COALESCE(ROUND(AVG(r.stability_score), 1), 0.0) AS average_stability_score,
                COALESCE(ROUND(AVG(r.recommend_score), 1), 0.0) AS average_recommend_score,
                COALESCE(ROUND(AVG(
                    (r.effect_score + r.ease_score + r.stability_score + r.recommend_score) / 4
                ), 1), 0.0) AS average_total_score
            FROM workflow_template t
            LEFT JOIN user_workflow_rating r
                ON r.template_id = t.id
               AND r.is_deleted = 0
            WHERE t.is_deleted = 0
            GROUP BY t.id, t.name
            ORDER BY t.sort_order ASC, t.id ASC
            """)
    List<WorkflowRatingStatisticsVO> selectWorkflowRatingSummary();
}
