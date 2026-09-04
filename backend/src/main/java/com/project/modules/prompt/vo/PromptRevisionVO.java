package com.project.modules.prompt.vo;

import com.project.modules.prompt.model.PromptParameterSnapshot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromptRevisionVO {

    private Long id;

    private Long promptId;

    private Integer revisionNo;

    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private String exampleInput;

    private String exampleOutput;

    private List<PromptParameterSnapshot> parameterSchema;

    private Long createdBy;

    private String status;

    private LocalDateTime createTime;
}
