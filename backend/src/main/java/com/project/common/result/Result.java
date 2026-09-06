package com.project.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    /** Stable machine-readable failure code. Null on successful and legacy responses. */
    private String errorCode;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, null);
    }

    public static <T> Result<T> failed(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, null);
    }

    public static <T> Result<T> failed(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null, null);
    }

    public static <T> Result<T> failed(Integer code, String message) {
        return new Result<>(code, message, null, null);
    }

    public static <T> Result<T> failed(Integer code, String errorCode, String message) {
        return new Result<>(code, message, null, errorCode);
    }
}
