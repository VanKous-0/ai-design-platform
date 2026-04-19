package com.project.modules.auth.service;

import com.project.modules.auth.dto.LoginRequest;
import com.project.modules.auth.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
