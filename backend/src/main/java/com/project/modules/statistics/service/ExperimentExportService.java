package com.project.modules.statistics.service;

import java.time.LocalDate;

public interface ExperimentExportService {

    byte[] exportUsers(String experimentBatch, String experimentGroup);

    byte[] exportUsageEvents(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    );

    byte[] exportWorkflowRecords(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    );

    byte[] exportWorkflowIterations(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    );

    byte[] exportRatings(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    );

    byte[] exportSurveyFeedback(
            String experimentBatch,
            String experimentGroup,
            LocalDate startDate,
            LocalDate endDate
    );
}
