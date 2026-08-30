package com.project.modules.user.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExperimentUserVO {

    private Long id;

    private String username;

    private String nickname;

    private String experimentCode;

    private String experimentGroup;

    private String experimentBatch;

    private Integer status;

    private LocalDateTime createTime;
}
