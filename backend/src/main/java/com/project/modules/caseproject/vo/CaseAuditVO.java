package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseAuditVO {

    private Long id;

    private String title;

    private String code;

    private Long submitUserId;

    private String summary;

    private String auditStatus;

    private String auditComment;

    private LocalDateTime auditTime;

    private Long auditorId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
