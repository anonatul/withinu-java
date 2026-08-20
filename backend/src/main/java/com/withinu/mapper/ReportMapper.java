package com.withinu.mapper;

import com.withinu.dto.ReportResponse;
import com.withinu.entity.Report;

public final class ReportMapper {

    private ReportMapper() {
    }

    public static ReportResponse toResponse(Report report) {
        String content = report.getMessage().getContent();
        String preview = content == null ? null
            : content.length() > 80 ? content.substring(0, 80) + "..." : content;
        return new ReportResponse(
            report.getId(),
            report.getMessage().getId(),
            preview,
            report.getReason(),
            report.getStatus(),
            report.getCreatedAt()
        );
    }
}