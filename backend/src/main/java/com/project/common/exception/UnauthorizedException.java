package com.project.common.exception;

import com.project.common.result.ResultCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final Integer code = ResultCode.UNAUTHORIZED.getCode();

    public UnauthorizedException(String message) {
        super(message);
    }
}
