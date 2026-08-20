package com.withinu.controller;

import com.withinu.dto.ReportRequest;
import com.withinu.dto.ReportResponse;
import com.withinu.security.AuthPrincipal;
import com.withinu.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports", description = "User moderation reports")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ModerationService moderationService;

    @Operation(summary = "Report a message for moderation")
    @PostMapping
    public ReportResponse reportMessage(@Valid @RequestBody ReportRequest request,
                                        @AuthenticationPrincipal AuthPrincipal principal) {
        return moderationService.reportMessage(request, principal.id());
    }
}