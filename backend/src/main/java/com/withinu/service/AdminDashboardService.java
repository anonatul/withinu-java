package com.withinu.service;

import com.withinu.dto.DashboardResponse;
import com.withinu.dto.ReportResponse;
import com.withinu.mapper.ReportMapper;
import com.withinu.moderation.ReportStatus;
import com.withinu.repository.MessageRepository;
import com.withinu.repository.ReportRepository;
import com.withinu.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;

    public DashboardResponse getDashboard() {
        long totalRooms = roomRepository.count();
        long activeRooms = roomRepository.countByActiveTrue();
        long totalMessages = messageRepository.count();
        long activeUsers = messageRepository.countDistinctActiveUsers();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long deletedMessages = messageRepository.countByDeletedTrue();
        List<ReportResponse> recentReports = reportRepository.findTop10ByOrderByCreatedAtDesc()
            .stream().map(ReportMapper::toResponse).toList();

        return new DashboardResponse(totalRooms, activeRooms, totalMessages, activeUsers,
            pendingReports, deletedMessages, recentReports);
    }
}