package com.project.modules.statistics.service.impl;

import com.project.common.exception.BusinessException;
import com.project.modules.statistics.mapper.StatisticsQueryMapper;
import com.project.modules.statistics.service.StatisticsService;
import com.project.modules.statistics.vo.EventTypeSummaryVO;
import com.project.modules.statistics.vo.PromptUsageSummaryVO;
import com.project.modules.statistics.vo.TargetTypeSummaryVO;
import com.project.modules.statistics.vo.ToolRatingStatisticsVO;
import com.project.modules.statistics.vo.UsageSummaryVO;
import com.project.modules.statistics.vo.WorkflowRatingStatisticsVO;
import com.project.modules.statistics.vo.WorkflowStatisticsVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsQueryMapper statisticsQueryMapper;

    public StatisticsServiceImpl(StatisticsQueryMapper statisticsQueryMapper) {
        this.statisticsQueryMapper = statisticsQueryMapper;
    }

    @Override
    public UsageSummaryVO getUsageSummary(LocalDate startDate, LocalDate endDate) {
        DateRange range = toDateRange(startDate, endDate);
        return statisticsQueryMapper.selectUsageSummary(range.startTime(), range.endTime());
    }

    @Override
    public List<EventTypeSummaryVO> getEventTypeSummary(LocalDate startDate, LocalDate endDate) {
        DateRange range = toDateRange(startDate, endDate);
        return statisticsQueryMapper.selectEventTypeSummary(range.startTime(), range.endTime());
    }

    @Override
    public List<TargetTypeSummaryVO> getTargetTypeSummary(LocalDate startDate, LocalDate endDate) {
        DateRange range = toDateRange(startDate, endDate);
        return statisticsQueryMapper.selectTargetTypeSummary(range.startTime(), range.endTime());
    }

    @Override
    public List<PromptUsageSummaryVO> getPromptSummary(LocalDate startDate, LocalDate endDate) {
        DateRange range = toDateRange(startDate, endDate);
        return statisticsQueryMapper.selectPromptSummary(range.startTime(), range.endTime());
    }

    @Override
    public List<WorkflowStatisticsVO> getWorkflowSummary() {
        return statisticsQueryMapper.selectWorkflowSummary();
    }

    @Override
    public List<ToolRatingStatisticsVO> getToolRatingSummary() {
        return statisticsQueryMapper.selectToolRatingSummary();
    }

    @Override
    public List<WorkflowRatingStatisticsVO> getWorkflowRatingSummary() {
        return statisticsQueryMapper.selectWorkflowRatingSummary();
    }

    private DateRange toDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("startDate不能晚于endDate");
        }
        LocalDateTime startTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endTime = endDate == null ? null : endDate.atTime(LocalTime.MAX);
        return new DateRange(startTime, endTime);
    }

    private record DateRange(LocalDateTime startTime, LocalDateTime endTime) {
    }
}
