package com.project.modules.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.prompt.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
