package com.project.modules.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.prompt.entity.PromptPreferenceHint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PromptPreferenceHintMapper extends BaseMapper<PromptPreferenceHint> {

    @Delete("DELETE FROM prompt_preference_hint WHERE prompt_id = #{promptId}")
    int hardDeleteByPromptId(@Param("promptId") Long promptId);
}
