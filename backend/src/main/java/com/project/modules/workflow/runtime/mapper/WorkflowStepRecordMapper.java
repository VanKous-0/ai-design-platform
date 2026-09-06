package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkflowStepRecordMapper extends BaseMapper<WorkflowStepRecord> {

    @Select("""
            SELECT * FROM workflow_step_record
            WHERE instance_id = #{instanceId}
              AND completion_request_id = #{requestId}
              AND is_deleted = 0
            LIMIT 1
            """)
    WorkflowStepRecord selectByCompletionRequest(
            @Param("instanceId") Long instanceId,
            @Param("requestId") String requestId
    );
}
