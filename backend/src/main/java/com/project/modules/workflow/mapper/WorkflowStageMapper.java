package com.project.modules.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowStageMapper extends BaseMapper<WorkflowStage> {
}
