package com.project.modules.statistics.service;

import com.project.modules.statistics.vo.EventTypeSummaryVO;
import com.project.modules.statistics.vo.PromptUsageSummaryVO;
import com.project.modules.statistics.vo.TargetTypeSummaryVO;
import com.project.modules.statistics.vo.ToolRatingStatisticsVO;
import com.project.modules.statistics.vo.UsageSummaryVO;
import com.project.modules.statistics.vo.WorkflowRatingStatisticsVO;
import com.project.modules.statistics.vo.WorkflowStatisticsVO;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    UsageSummaryVO getUsageSummary(LocalDate startDate, LocalDate endDate);

    List<EventTypeSummaryVO> getEventTypeSummary(LocalDate startDate, LocalDate endDate);

    List<TargetTypeSummaryVO> getTargetTypeSummary(LocalDate startDate, LocalDate endDate);

    List<PromptUsageSummaryVO> getPromptSummary(LocalDate startDate, LocalDate endDate);

    List<WorkflowStatisticsVO> getWorkflowSummary();

    List<ToolRatingStatisticsVO> getToolRatingSummary();

    List<WorkflowRatingStatisticsVO> getWorkflowRatingSummary();
}
