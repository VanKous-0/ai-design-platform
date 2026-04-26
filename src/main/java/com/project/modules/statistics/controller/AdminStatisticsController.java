package com.project.modules.statistics.controller;

import com.project.common.result.Result;
import com.project.modules.statistics.service.StatisticsService;
import com.project.modules.statistics.vo.EventTypeSummaryVO;
import com.project.modules.statistics.vo.PromptUsageSummaryVO;
import com.project.modules.statistics.vo.TargetTypeSummaryVO;
import com.project.modules.statistics.vo.ToolRatingStatisticsVO;
import com.project.modules.statistics.vo.UsageSummaryVO;
import com.project.modules.statistics.vo.WorkflowRatingStatisticsVO;
import com.project.modules.statistics.vo.WorkflowStatisticsVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/usage-summary")
    public Result<UsageSummaryVO> getUsageSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return Result.success(statisticsService.getUsageSummary(startDate, endDate));
    }

    @GetMapping("/event-type-summary")
    public Result<List<EventTypeSummaryVO>> getEventTypeSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return Result.success(statisticsService.getEventTypeSummary(startDate, endDate));
    }

    @GetMapping("/target-type-summary")
    public Result<List<TargetTypeSummaryVO>> getTargetTypeSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return Result.success(statisticsService.getTargetTypeSummary(startDate, endDate));
    }

    @GetMapping("/prompt-summary")
    public Result<List<PromptUsageSummaryVO>> getPromptSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return Result.success(statisticsService.getPromptSummary(startDate, endDate));
    }

    @GetMapping("/workflow-summary")
    public Result<List<WorkflowStatisticsVO>> getWorkflowSummary() {
        return Result.success(statisticsService.getWorkflowSummary());
    }

    @GetMapping("/tool-rating-summary")
    public Result<List<ToolRatingStatisticsVO>> getToolRatingSummary() {
        return Result.success(statisticsService.getToolRatingSummary());
    }

    @GetMapping("/workflow-rating-summary")
    public Result<List<WorkflowRatingStatisticsVO>> getWorkflowRatingSummary() {
        return Result.success(statisticsService.getWorkflowRatingSummary());
    }
}
