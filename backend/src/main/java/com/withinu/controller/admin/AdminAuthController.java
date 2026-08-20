package com.withinu.controller.admin;

import com.withinu.dto.AdminLoginRequest;
import com.withinu.dto.AdminLoginResponse;
import com.withinu.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth", description = "Administrator authentication")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "Login with admin credentials")
    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }
}