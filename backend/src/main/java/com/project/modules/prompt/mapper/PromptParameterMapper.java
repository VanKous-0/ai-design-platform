package com.project.modules.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.prompt.entity.PromptParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PromptParameterMapper extends BaseMapper<PromptParameter> {

    @Delete("DELETE FROM prompt_parameter WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM prompt_parameter WHERE prompt_id = #{promptId}")
    int hardDeleteByPromptId(@Param("promptId") Long promptId);
}
