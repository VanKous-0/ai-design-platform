package com.project.modules.statistics.controller;

import com.project.common.result.Result;
import com.project.modules.statistics.service.SurveyFeedbackService;
import com.project.modules.statistics.vo.SurveyFeedbackVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/survey-feedback")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSurveyFeedbackController {

    private final SurveyFeedbackService surveyFeedbackService;

    public AdminSurveyFeedbackController(SurveyFeedbackService surveyFeedbackService) {
        this.surveyFeedbackService = surveyFeedbackService;
    }

    @GetMapping
    public Result<List<SurveyFeedbackVO>> listFeedback() {
        return Result.success(surveyFeedbackService.listFeedback());
    }
}
