package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowStepIteration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkflowStepIterationMapper extends BaseMapper<WorkflowStepIteration> {

    @Select("""
            SELECT * FROM workflow_step_iteration
            WHERE instance_id = #{instanceId}
              AND creation_request_id = #{requestId}
              AND is_deleted = 0
            LIMIT 1
            """)
    WorkflowStepIteration selectByCreationRequest(
            @Param("instanceId") Long instanceId,
            @Param("requestId") String requestId
    );
}
