package com.project.modules.auth.vo;

import com.project.modules.user.vo.UserInfoVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String tokenType;

    private String accessToken;

    private Long expiresIn;

    private UserInfoVO user;
}
