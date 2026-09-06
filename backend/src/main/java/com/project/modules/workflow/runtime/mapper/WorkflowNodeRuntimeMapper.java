package com.project.modules.workflow.runtime.mapper;

import com.project.modules.workflow.runtime.entity.WorkflowNodeRuntime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkflowNodeRuntimeMapper {

    @Insert("""
            INSERT IGNORE INTO workflow_node_runtime
                (instance_id, node_id, next_iteration_no, lock_version, create_time, update_time)
            VALUES (#{instanceId}, #{nodeId}, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """)
    int insertIfAbsent(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    @Select("""
            SELECT instance_id, node_id, next_iteration_no, selected_iteration_id,
                   lock_version, create_time, update_time
            FROM workflow_node_runtime
            WHERE instance_id = #{instanceId} AND node_id = #{nodeId}
            FOR UPDATE
            """)
    WorkflowNodeRuntime selectForUpdate(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    @Select("""
            SELECT instance_id, node_id, next_iteration_no, selected_iteration_id,
                   lock_version, create_time, update_time
            FROM workflow_node_runtime
            WHERE instance_id = #{instanceId} AND node_id = #{nodeId}
            """)
    WorkflowNodeRuntime selectRuntime(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    @Update("""
            UPDATE workflow_node_runtime
            SET next_iteration_no = next_iteration_no + 1, update_time = CURRENT_TIMESTAMP
            WHERE instance_id = #{instanceId} AND node_id = #{nodeId}
              AND next_iteration_no = #{allocatedNo}
            """)
    int advanceIterationCounter(
            @Param("instanceId") Long instanceId,
            @Param("nodeId") Long nodeId,
            @Param("allocatedNo") Integer allocatedNo
    );

    @Update("""
            UPDATE workflow_node_runtime
            SET selected_iteration_id = #{iterationId},
                lock_version = lock_version + 1,
                update_time = CURRENT_TIMESTAMP
            WHERE instance_id = #{instanceId} AND node_id = #{nodeId}
            """)
    int setSelectedIteration(
            @Param("instanceId") Long instanceId,
            @Param("nodeId") Long nodeId,
            @Param("iterationId") Long iterationId
    );
}
