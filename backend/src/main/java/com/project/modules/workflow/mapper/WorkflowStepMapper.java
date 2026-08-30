package com.project.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.entity.WorkflowStep;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowStepMapper extends BaseMapper<WorkflowStep> {
}
