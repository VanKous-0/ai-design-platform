package com.project.modules.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExperimentUserCredentialVO {

    private Long id;

    private String username;

    private String experimentCode;

    private String experimentGroup;

    private String experimentBatch;

    private String initialPassword;
}
