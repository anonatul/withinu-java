package com.withinu.service;

import com.withinu.dto.AdminLoginRequest;
import com.withinu.dto.AdminLoginResponse;
import com.withinu.entity.AdminUser;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.repository.AdminUserRepository;
import com.withinu.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.username())
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
        }
        String token = jwtService.issueAdminToken(admin.getId());
        return new AdminLoginResponse(true, token, jwtService.adminTokenTtlSeconds(), admin.getUsername());
    }
}