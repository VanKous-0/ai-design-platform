package com.project.modules.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.statistics.entity.UsageEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UsageEventMapper extends BaseMapper<UsageEvent> {

    @Select("""
            SELECT * FROM usage_event
            WHERE idempotency_actor = #{actor}
              AND idempotency_key = #{key}
              AND is_deleted = 0
            LIMIT 1
            """)
    UsageEvent selectIdempotent(@Param("actor") String actor, @Param("key") String key);
}
