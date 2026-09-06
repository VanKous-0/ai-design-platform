package com.project.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DomainError {

    WORKFLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "Workflow resource was not found"),
    WORKFLOW_NOT_OWNED(HttpStatus.FORBIDDEN, "Workflow resource is owned by another user"),
    WORKFLOW_ALREADY_FINISHED(HttpStatus.CONFLICT, "Workflow instance has already finished"),
    WORKFLOW_NODE_NOT_CURRENT(HttpStatus.CONFLICT, "Workflow node is not the current node"),
    WORKFLOW_STATE_CONFLICT(HttpStatus.CONFLICT, "Workflow state changed concurrently"),
    WORKFLOW_ITERATION_CONFLICT(HttpStatus.CONFLICT, "Workflow iteration changed concurrently"),
    PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "Prompt was not found"),
    PROMPT_REVISION_NOT_FOUND(HttpStatus.NOT_FOUND, "Prompt revision was not found"),
    PROMPT_REVISION_MISMATCH(HttpStatus.CONFLICT, "Prompt revision does not belong to the prompt"),
    PROFILE_INVALID_SOURCE(HttpStatus.BAD_REQUEST, "Preference source is invalid for this operation"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource conflicts with existing state"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access is forbidden");

    private final HttpStatus status;
    private final String defaultMessage;

    DomainError(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
