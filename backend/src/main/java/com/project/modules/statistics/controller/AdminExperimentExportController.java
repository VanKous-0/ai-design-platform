package com.project.modules.statistics.controller;

import com.project.modules.statistics.service.ExperimentExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/exports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExperimentExportController {

    private final ExperimentExportService exportService;

    public AdminExperimentExportController(ExperimentExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/users.csv")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup
    ) {
        return csv("experiment-users.csv", exportService.exportUsers(experimentBatch, experimentGroup));
    }

    @GetMapping("/usage-events.csv")
    public ResponseEntity<byte[]> exportUsageEvents(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return csv("usage-events.csv", exportService.exportUsageEvents(
                experimentBatch, experimentGroup, startDate, endDate
        ));
    }

    @GetMapping("/workflow-records.csv")
    public ResponseEntity<byte[]> exportWorkflowRecords(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return csv("workflow-records.csv", exportService.exportWorkflowRecords(
                experimentBatch, experimentGroup, startDate, endDate
        ));
    }

    @GetMapping("/workflow-iterations.csv")
    public ResponseEntity<byte[]> exportWorkflowIterations(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return csv("workflow-iterations.csv", exportService.exportWorkflowIterations(
                experimentBatch, experimentGroup, startDate, endDate
        ));
    }

    @GetMapping("/ratings.csv")
    public ResponseEntity<byte[]> exportRatings(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return csv("ratings.csv", exportService.exportRatings(
                experimentBatch, experimentGroup, startDate, endDate
        ));
    }

    @GetMapping("/survey-feedback.csv")
    public ResponseEntity<byte[]> exportSurveyFeedback(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return csv("survey-feedback.csv", exportService.exportSurveyFeedback(
                experimentBatch, experimentGroup, startDate, endDate
        ));
    }

    private ResponseEntity<byte[]> csv(String filename, byte[] content) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content);
    }
}
