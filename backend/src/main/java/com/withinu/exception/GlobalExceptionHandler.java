package com.withinu.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        return build(ex.getErrorCode().getStatus(), ex.getErrorCode().name(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException mv) {
            Map<String, String> fields = new LinkedHashMap<>();
            mv.getBindingResult().getFieldErrors().forEach(fe ->
                fields.put(fe.getField(), fe.getDefaultMessage()));
            return buildWithDetails(ErrorCode.VALIDATION_ERROR.getStatus(), ErrorCode.VALIDATION_ERROR.name(),
                "Validation failed", fields);
        }
        if (ex instanceof ConstraintViolationException cv) {
            Map<String, String> fields = new LinkedHashMap<>();
            cv.getConstraintViolations().forEach(v ->
                fields.put(v.getPropertyPath().toString(), v.getMessage()));
            return buildWithDetails(ErrorCode.VALIDATION_ERROR.getStatus(), ErrorCode.VALIDATION_ERROR.name(),
                "Validation failed", fields);
        }
        return build(ErrorCode.VALIDATION_ERROR.getStatus(), ErrorCode.VALIDATION_ERROR.name(),
            "Malformed request body");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return build(ErrorCode.FORBIDDEN.getStatus(), ErrorCode.FORBIDDEN.name(), "Access denied");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(ErrorCode.INTERNAL_ERROR.getStatus(), ErrorCode.INTERNAL_ERROR.name(),
            "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String errorCode, String message) {
        return buildWithDetails(status, errorCode, message, null);
    }

    private ResponseEntity<Map<String, Object>> buildWithDetails(HttpStatus status, String errorCode, String message, Object details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("errorCode", errorCode);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        if (details != null) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}