package com.withinu.dto;

import com.withinu.moderation.ReportReason;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportRequest(
    @NotNull(message = "messageId is required")
    UUID messageId,

    @NotNull(message = "reason is required")
    ReportReason reason
) {
}