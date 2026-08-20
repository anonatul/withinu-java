package com.withinu.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    OUTSIDE_CAMPUS(HttpStatus.FORBIDDEN),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}