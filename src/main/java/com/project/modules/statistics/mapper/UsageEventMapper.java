package com.project.modules.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.statistics.entity.UsageEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsageEventMapper extends BaseMapper<UsageEvent> {
}
