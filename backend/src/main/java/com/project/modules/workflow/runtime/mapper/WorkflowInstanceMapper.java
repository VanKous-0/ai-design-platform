package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {
}
