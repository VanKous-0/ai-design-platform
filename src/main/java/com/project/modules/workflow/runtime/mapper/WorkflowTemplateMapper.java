package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowTemplateMapper extends BaseMapper<WorkflowTemplate> {
}
