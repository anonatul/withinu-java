package com.withinu.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
    @NotBlank(message = "username is required")
    String username,

    @NotBlank(message = "password is required")
    String password
) {
}