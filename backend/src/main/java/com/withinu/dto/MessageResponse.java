package com.withinu.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID roomId,
    String displayName,
    String content,
    boolean deleted,
    boolean mine,
    Instant createdAt
) {
}