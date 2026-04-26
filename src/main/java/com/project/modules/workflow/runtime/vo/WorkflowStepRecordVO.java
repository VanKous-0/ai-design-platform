package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowStepRecordVO {

    private Long id;

    private Long instanceId;

    private Long nodeId;

    private String nodeName;

    private String inputContent;

    private String outputContent;

    private String status;

    private Integer durationSeconds;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String nextSuggestion;
}
