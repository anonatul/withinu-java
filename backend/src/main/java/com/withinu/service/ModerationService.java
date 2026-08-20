package com.withinu.service;

import com.withinu.dto.PageResponse;
import com.withinu.dto.ReportRequest;
import com.withinu.dto.ReportResponse;
import com.withinu.entity.AdminUser;
import com.withinu.entity.AnonymousUser;
import com.withinu.entity.Message;
import com.withinu.entity.Report;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.mapper.ReportMapper;
import com.withinu.moderation.ReportStatus;
import com.withinu.repository.AnonymousUserRepository;
import com.withinu.repository.MessageRepository;
import com.withinu.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ReportRepository reportRepository;
    private final MessageRepository messageRepository;
    private final AnonymousUserRepository userRepository;

    @Transactional
    public ReportResponse reportMessage(ReportRequest request, UUID reporterId) {
        Message message = messageRepository.findById(request.messageId())
            .orElseThrow(() -> new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found"));
        if (message.isDeleted()) {
            throw new ApiException(ErrorCode.MESSAGE_NOT_FOUND, "Message not found");
        }
        AnonymousUser reporter = userRepository.findById(reporterId)
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Unknown anonymous user"));

        Report report = Report.builder()
            .message(message)
            .reportedBy(reporter)
            .reason(request.reason())
            .status(ReportStatus.PENDING)
            .createdAt(Instant.now())
            .build();
        return ReportMapper.toResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> listReports(ReportStatus status, int page, int size) {
        int pageSize = Math.min(size, 50);
        Page<Report> result = reportRepository.findByStatusOrderByCreatedAtDesc(
            status, PageRequest.of(Math.max(page, 0), pageSize));
        List<ReportResponse> content = result.getContent().stream()
            .map(ReportMapper::toResponse)
            .toList();
        return new PageResponse<>(content, result.getNumber(), pageSize,
            result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    @Transactional
    public ReportResponse resolveReport(UUID reportId, ReportStatus status, AdminUser admin) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ApiException(ErrorCode.FORBIDDEN, "Report not found"));
        report.setStatus(status);
        report.setResolvedAt(Instant.now());
        report.setResolvedBy(admin);
        return ReportMapper.toResponse(reportRepository.save(report));
    }
}