package com.withinu.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageRequest(
    @NotNull(message = "roomId is required")
    UUID roomId,

    @NotNull(message = "content is required")
    String content
) {
}