package com.project.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.rating.entity.UserToolRating;
import com.project.modules.rating.entity.UserWorkflowRating;
import com.project.modules.rating.mapper.UserToolRatingMapper;
import com.project.modules.rating.mapper.UserWorkflowRatingMapper;
import com.project.modules.statistics.entity.UsageEvent;
import com.project.modules.statistics.mapper.UsageEventMapper;
import com.project.modules.statistics.service.StatisticsService;
import com.project.modules.statistics.vo.EventTypeSummaryVO;
import com.project.modules.statistics.vo.PromptUsageSummaryVO;
import com.project.modules.statistics.vo.TargetTypeSummaryVO;
import com.project.modules.statistics.vo.ToolRatingStatisticsVO;
import com.project.modules.statistics.vo.UsageSummaryVO;
import com.project.modules.statistics.vo.WorkflowRatingStatisticsVO;
import com.project.modules.statistics.vo.WorkflowStatisticsVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

    private final UsageEventMapper usageEventMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowStepRecordMapper workflowStepRecordMapper;
    private final AiToolMapper aiToolMapper;
    private final UserToolRatingMapper userToolRatingMapper;
    private final UserWorkflowRatingMapper userWorkflowRatingMapper;

    public StatisticsServiceImpl(
            UsageEventMapper usageEventMapper,
            PromptTemplateMapper promptTemplateMapper,
            WorkflowTemplateMapper workflowTemplateMapper,
            WorkflowInstanceMapper workflowInstanceMapper,
            WorkflowStepRecordMapper workflowStepRecordMapper,
            AiToolMapper aiToolMapper,
            UserToolRatingMapper userToolRatingMapper,
            UserWorkflowRatingMapper userWorkflowRatingMapper
    ) {
        this.usageEventMapper = usageEventMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowInstanceMapper = workflowInstanceMapper;
        this.workflowStepRecordMapper = workflowStepRecordMapper;
        this.aiToolMapper = aiToolMapper;
        this.userToolRatingMapper = userToolRatingMapper;
        this.userWorkflowRatingMapper = userWorkflowRatingMapper;
    }

    @Override
    public UsageSummaryVO getUsageSummary(LocalDate startDate, LocalDate endDate) {
        List<UsageEvent> events = listUsageEvents(startDate, endDate);
        long totalStayDuration = events.stream()
                .map(UsageEvent::getStayDuration)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long stayDurationCount = events.stream()
                .map(UsageEvent::getStayDuration)
                .filter(Objects::nonNull)
                .count();
        return UsageSummaryVO.builder()
                .totalEventCount((long) events.size())
                .loginUserEventCount(events.stream().filter(event -> event.getUserId() != null).count())
                .anonymousEventCount(events.stream().filter(event -> event.getUserId() == null).count())
                .uniqueUserCount(events.stream().map(UsageEvent::getUserId).filter(Objects::nonNull).distinct().count())
                .uniqueAnonymousCount(events.stream().map(UsageEvent::getAnonymousId).filter(this::hasText).distinct().count())
                .totalStayDuration(totalStayDuration)
                .averageStayDuration(stayDurationCount == 0 ? ZERO : BigDecimal.valueOf(totalStayDuration)
                        .divide(BigDecimal.valueOf(stayDurationCount), 1, RoundingMode.HALF_UP))
                .build();
    }

    @Override
    public List<EventTypeSummaryVO> getEventTypeSummary(LocalDate startDate, LocalDate endDate) {
        return listUsageEvents(startDate, endDate).stream()
                .filter(event -> hasText(event.getEventType()))
                .collect(Collectors.groupingBy(UsageEvent::getEventType, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> EventTypeSummaryVO.builder().eventType(entry.getKey()).count(entry.getValue()).build())
                .toList();
    }

    @Override
    public List<TargetTypeSummaryVO> getTargetTypeSummary(LocalDate startDate, LocalDate endDate) {
        return listUsageEvents(startDate, endDate).stream()
                .filter(event -> hasText(event.getTargetType()))
                .collect(Collectors.groupingBy(UsageEvent::getTargetType, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> TargetTypeSummaryVO.builder().targetType(entry.getKey()).count(entry.getValue()).build())
                .toList();
    }

    @Override
    public List<PromptUsageSummaryVO> getPromptSummary(LocalDate startDate, LocalDate endDate) {
        List<UsageEvent> promptEvents = listUsageEvents(startDate, endDate).stream()
                .filter(event -> Objects.equals(event.getTargetType(), "prompt"))
                .filter(event -> event.getTargetId() != null)
                .toList();
        Map<Long, Long> eventCounts = promptEvents.stream()
                .collect(Collectors.groupingBy(UsageEvent::getTargetId, Collectors.counting()));
        Map<Long, Long> renderCounts = promptEvents.stream()
                .filter(event -> Objects.equals(event.getEventType(), "render_prompt"))
                .collect(Collectors.groupingBy(UsageEvent::getTargetId, Collectors.counting()));
        return promptTemplateMapper.selectList(new LambdaQueryWrapper<PromptTemplate>()
                        .orderByAsc(PromptTemplate::getSortOrder)
                        .orderByAsc(PromptTemplate::getId))
                .stream()
                .map(prompt -> PromptUsageSummaryVO.builder()
                        .promptId(prompt.getId())
                        .promptTitle(prompt.getTitle())
                        .copyCount(prompt.getCopyCount())
                        .renderCount(renderCounts.getOrDefault(prompt.getId(), 0L))
                        .eventCount(eventCounts.getOrDefault(prompt.getId(), 0L))
                        .build())
                .toList();
    }

    @Override
    public List<WorkflowStatisticsVO> getWorkflowSummary() {
        List<WorkflowInstance> instances = workflowInstanceMapper.selectList(new LambdaQueryWrapper<>());
        List<WorkflowStepRecord> stepRecords = workflowStepRecordMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, List<WorkflowInstance>> instancesByTemplate = instances.stream()
                .collect(Collectors.groupingBy(WorkflowInstance::getTemplateId));
        Map<Long, Long> completedStepsByTemplate = stepRecords.stream()
                .map(record -> workflowInstanceMapper.selectById(record.getInstanceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(WorkflowInstance::getTemplateId, Collectors.counting()));

        return workflowTemplateMapper.selectList(new LambdaQueryWrapper<WorkflowTemplate>()
                        .orderByAsc(WorkflowTemplate::getSortOrder)
                        .orderByAsc(WorkflowTemplate::getId))
                .stream()
                .map(template -> {
                    List<WorkflowInstance> templateInstances = instancesByTemplate.getOrDefault(template.getId(), Collections.emptyList());
                    return WorkflowStatisticsVO.builder()
                            .templateId(template.getId())
                            .templateName(template.getName())
                            .instanceCount((long) templateInstances.size())
                            .runningCount(templateInstances.stream().filter(instance -> Objects.equals(instance.getStatus(), "RUNNING")).count())
                            .finishedCount(templateInstances.stream().filter(instance -> Objects.equals(instance.getStatus(), "FINISHED")).count())
                            .averageProgress(average(templateInstances.stream()
                                    .map(WorkflowInstance::getProgress)
                                    .filter(Objects::nonNull)
                                    .toList()))
                            .completeStepCount(completedStepsByTemplate.getOrDefault(template.getId(), 0L))
                            .build();
                })
                .toList();
    }

    @Override
    public List<ToolRatingStatisticsVO> getToolRatingSummary() {
        Map<Long, List<UserToolRating>> ratingsByTool = userToolRatingMapper.selectList(new LambdaQueryWrapper<UserToolRating>())
                .stream()
                .collect(Collectors.groupingBy(UserToolRating::getToolId));
        return aiToolMapper.selectList(new LambdaQueryWrapper<AiTool>().orderByAsc(AiTool::getId))
                .stream()
                .map(tool -> toToolRatingStatistics(tool, ratingsByTool.getOrDefault(tool.getId(), Collections.emptyList())))
                .sorted(Comparator.comparing(ToolRatingStatisticsVO::getRatingCount).reversed())
                .toList();
    }

    @Override
    public List<WorkflowRatingStatisticsVO> getWorkflowRatingSummary() {
        Map<Long, List<UserWorkflowRating>> ratingsByTemplate = userWorkflowRatingMapper.selectList(new LambdaQueryWrapper<UserWorkflowRating>())
                .stream()
                .collect(Collectors.groupingBy(UserWorkflowRating::getTemplateId));
        return workflowTemplateMapper.selectList(new LambdaQueryWrapper<WorkflowTemplate>()
                        .orderByAsc(WorkflowTemplate::getSortOrder)
                        .orderByAsc(WorkflowTemplate::getId))
                .stream()
                .map(template -> toWorkflowRatingStatistics(template, ratingsByTemplate.getOrDefault(template.getId(), Collections.emptyList())))
                .toList();
    }

    private List<UsageEvent> listUsageEvents(LocalDate startDate, LocalDate endDate) {
        DateRange range = toDateRange(startDate, endDate);
        LambdaQueryWrapper<UsageEvent> query = new LambdaQueryWrapper<UsageEvent>()
                .orderByDesc(UsageEvent::getCreateTime);
        if (range.startTime != null) {
            query.ge(UsageEvent::getCreateTime, range.startTime);
        }
        if (range.endTime != null) {
            query.le(UsageEvent::getCreateTime, range.endTime);
        }
        return usageEventMapper.selectList(query);
    }

    private DateRange toDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("startDate cannot be after endDate");
        }
        LocalDateTime startTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endTime = endDate == null ? null : endDate.atTime(LocalTime.MAX);
        return new DateRange(startTime, endTime);
    }

    private ToolRatingStatisticsVO toToolRatingStatistics(AiTool tool, List<UserToolRating> ratings) {
        BigDecimal effect = average(ratings.stream().map(UserToolRating::getEffectScore).toList());
        BigDecimal ease = average(ratings.stream().map(UserToolRating::getEaseScore).toList());
        BigDecimal stability = average(ratings.stream().map(UserToolRating::getStabilityScore).toList());
        BigDecimal recommend = average(ratings.stream().map(UserToolRating::getRecommendScore).toList());
        return ToolRatingStatisticsVO.builder()
                .toolId(tool.getId())
                .toolName(tool.getName())
                .ratingCount((long) ratings.size())
                .averageEffectScore(effect)
                .averageEaseScore(ease)
                .averageStabilityScore(stability)
                .averageRecommendScore(recommend)
                .averageTotalScore(average(List.of(effect, ease, stability, recommend)))
                .build();
    }

    private WorkflowRatingStatisticsVO toWorkflowRatingStatistics(WorkflowTemplate template, List<UserWorkflowRating> ratings) {
        BigDecimal effect = average(ratings.stream().map(UserWorkflowRating::getEffectScore).toList());
        BigDecimal ease = average(ratings.stream().map(UserWorkflowRating::getEaseScore).toList());
        BigDecimal stability = average(ratings.stream().map(UserWorkflowRating::getStabilityScore).toList());
        BigDecimal recommend = average(ratings.stream().map(UserWorkflowRating::getRecommendScore).toList());
        return WorkflowRatingStatisticsVO.builder()
                .templateId(template.getId())
                .templateName(template.getName())
                .ratingCount((long) ratings.size())
                .averageEffectScore(effect)
                .averageEaseScore(ease)
                .averageStabilityScore(stability)
                .averageRecommendScore(recommend)
                .averageTotalScore(average(List.of(effect, ease, stability, recommend)))
                .build();
    }

    private BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> actualValues = values.stream().filter(Objects::nonNull).toList();
        if (actualValues.isEmpty()) {
            return ZERO;
        }
        return actualValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(actualValues.size()), 1, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record DateRange(LocalDateTime startTime, LocalDateTime endTime) {
    }
}
