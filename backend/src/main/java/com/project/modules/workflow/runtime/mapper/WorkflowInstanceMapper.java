package com.project.modules.workflow.runtime.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

    @Update("""
            UPDATE workflow_instance
            SET current_node_id = #{nextNodeId},
                status = #{nextStatus},
                finish_time = #{finishTime},
                progress = #{progress},
                lock_version = lock_version + 1,
                update_time = #{updateTime}
            WHERE id = #{instanceId}
              AND user_id = #{userId}
              AND current_node_id = #{expectedNodeId}
              AND status = 'RUNNING'
              AND lock_version = #{expectedVersion}
              AND is_deleted = 0
            """)
    int advanceCurrentNode(
            @Param("instanceId") Long instanceId,
            @Param("userId") Long userId,
            @Param("expectedNodeId") Long expectedNodeId,
            @Param("expectedVersion") Long expectedVersion,
            @Param("nextNodeId") Long nextNodeId,
            @Param("nextStatus") String nextStatus,
            @Param("finishTime") java.time.LocalDateTime finishTime,
            @Param("progress") java.math.BigDecimal progress,
            @Param("updateTime") java.time.LocalDateTime updateTime
    );
}
