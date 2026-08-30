package com.project.modules.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.modules.profile.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
