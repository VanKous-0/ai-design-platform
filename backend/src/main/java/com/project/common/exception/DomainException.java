package com.project.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final DomainError error;

    public DomainException(DomainError error) {
        this(error, error.getDefaultMessage());
    }

    public DomainException(DomainError error, String message) {
        super(message);
        this.error = error;
    }
}
