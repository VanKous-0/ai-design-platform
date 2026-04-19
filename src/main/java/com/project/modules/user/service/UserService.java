package com.project.modules.user.service;

import com.project.modules.user.vo.UserInfoVO;

public interface UserService {

    UserInfoVO getCurrentUser(Long userId);
}
