package com.withinu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AdminRoomRequest(
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name cannot exceed 100 characters")
    String name,

    @Size(max = 100, message = "slug cannot exceed 100 characters")
    String slug,

    @Size(max = 2000, message = "description cannot exceed 2000 characters")
    String description,

    @NotNull(message = "active is required")
    Boolean active
) {
}