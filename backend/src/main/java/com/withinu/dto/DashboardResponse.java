package com.withinu.dto;

import java.util.List;

public record DashboardResponse(
    long totalRooms,
    long activeRooms,
    long totalMessages,
    long activeUsers,
    long pendingReports,
    long deletedMessages,
    List<ReportResponse> recentReports
) {
}