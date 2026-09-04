package com.project.modules.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.prompt.entity.PromptRevision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromptRevisionMapper extends BaseMapper<PromptRevision> {

    @Select("SELECT COALESCE(MAX(revision_no), 0) FROM prompt_revision WHERE prompt_id = #{promptId}")
    int selectMaxRevisionNo(@Param("promptId") Long promptId);
}
