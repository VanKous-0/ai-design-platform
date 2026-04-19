package com.project.modules.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String role;
}
