package com.project.modules.tool.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolStageVO {

    private Long id;

    private String name;

    private String code;
}
