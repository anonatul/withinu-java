package com.withinu.dto;

import com.withinu.moderation.ReportReason;
import com.withinu.moderation.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
    UUID id,
    UUID messageId,
    String contentPreview,
    ReportReason reason,
    ReportStatus status,
    Instant createdAt
) {
}