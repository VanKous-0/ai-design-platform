package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowStepRecordMapper extends BaseMapper<WorkflowStepRecord> {
}
