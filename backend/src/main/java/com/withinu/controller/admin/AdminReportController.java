package com.withinu.controller.admin;

import com.withinu.dto.PageResponse;
import com.withinu.dto.ReportResponse;
import com.withinu.moderation.ReportStatus;
import com.withinu.security.AuthPrincipal;
import com.withinu.service.AdminContextService;
import com.withinu.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin Reports", description = "Moderation report handling")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ModerationService moderationService;
    private final AdminContextService adminContextService;

    @Operation(summary = "List reports by status (paginated)")
    @GetMapping
    public PageResponse<ReportResponse> listReports(
        @RequestParam(defaultValue = "PENDING") ReportStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "30") int size) {
        return moderationService.listReports(status, page, size);
    }

    @Operation(summary = "Resolve or dismiss a report")
    @PatchMapping("/{reportId}")
    public ReportResponse resolveReport(@PathVariable UUID reportId,
                                        @RequestParam ReportStatus status,
                                        @AuthenticationPrincipal AuthPrincipal principal) {
        return moderationService.resolveReport(reportId, status,
            adminContextService.getCurrentAdmin(principal));
    }
}