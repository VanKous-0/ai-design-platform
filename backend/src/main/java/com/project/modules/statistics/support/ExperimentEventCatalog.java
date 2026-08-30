package com.project.modules.statistics.support;

import com.project.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

public final class ExperimentEventCatalog {

    public static final String LOGIN = "login";
    public static final String VIEW_TOOL = "view_tool";
    public static final String SELECT_WORKFLOW_TEMPLATE = "select_workflow_template";
    public static final String RENDER_PROMPT = "render_prompt";
    public static final String COPY_PROMPT = "copy_prompt";
    public static final String COMPLETE_WORKFLOW_STEP = "complete_workflow_step";
    public static final String SUBMIT_TOOL_RATING = "submit_tool_rating";
    public static final String SUBMIT_WORKFLOW_RATING = "submit_workflow_rating";
    public static final String SUBMIT_SURVEY = "submit_survey";

    private static final Set<String> TARGET_TYPES = Set.of(
            "tool",
            "workflow_template",
            "workflow_instance",
            "workflow_node",
            "prompt",
            "survey"
    );

    private static final Map<String, Set<String>> EVENT_TARGETS = Map.of(
            LOGIN, Set.of(),
            VIEW_TOOL, Set.of("tool"),
            SELECT_WORKFLOW_TEMPLATE, Set.of("workflow_template"),
            RENDER_PROMPT, Set.of("prompt"),
            COPY_PROMPT, Set.of("prompt"),
            COMPLETE_WORKFLOW_STEP, Set.of("workflow_node", "workflow_instance"),
            SUBMIT_TOOL_RATING, Set.of("tool"),
            SUBMIT_WORKFLOW_RATING, Set.of("workflow_template", "workflow_instance"),
            SUBMIT_SURVEY, Set.of("survey")
    );

    private ExperimentEventCatalog() {
    }

    public static void validate(String eventType, String targetType, Long targetId) {
        Set<String> allowedTargets = EVENT_TARGETS.get(eventType);
        if (allowedTargets == null) {
            throw new BusinessException("不支持的实验事件类型: " + eventType);
        }
        if (!StringUtils.hasText(targetType)) {
            if (!allowedTargets.isEmpty()) {
                throw new BusinessException("事件" + eventType + "必须指定targetType");
            }
            return;
        }
        if (!TARGET_TYPES.contains(targetType) || !allowedTargets.contains(targetType)) {
            throw new BusinessException("事件" + eventType + "不支持目标类型" + targetType);
        }
        if (targetId == null && !LOGIN.equals(eventType) && !SUBMIT_SURVEY.equals(eventType)) {
            throw new BusinessException("事件" + eventType + "必须指定targetId");
        }
    }
}
