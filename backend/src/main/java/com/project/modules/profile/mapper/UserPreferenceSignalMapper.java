package com.project.modules.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.profile.entity.UserPreferenceSignal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPreferenceSignalMapper extends BaseMapper<UserPreferenceSignal> {

    @Delete("DELETE FROM user_preference_signal WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
