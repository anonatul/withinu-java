package com.withinu.dto;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(
    UUID id,
    String name,
    String slug,
    String description,
    long messageCount,
    Instant lastActivity
) {
}