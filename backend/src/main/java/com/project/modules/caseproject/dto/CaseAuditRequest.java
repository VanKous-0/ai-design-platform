package com.project.modules.caseproject.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CaseAuditRequest {

    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String auditComment;
}
