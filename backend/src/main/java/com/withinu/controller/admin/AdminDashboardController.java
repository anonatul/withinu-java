package com.withinu.controller.admin;

import com.withinu.dto.DashboardResponse;
import com.withinu.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Dashboard", description = "Moderation overview statistics")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Get moderation dashboard statistics")
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminDashboardService.getDashboard();
    }
}