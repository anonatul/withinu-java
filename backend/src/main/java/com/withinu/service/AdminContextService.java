package com.withinu.service;

import com.withinu.entity.AdminUser;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.repository.AdminUserRepository;
import com.withinu.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminContextService {

    private final AdminUserRepository adminUserRepository;

    public AdminUser getCurrentAdmin(AuthPrincipal principal) {
        if (principal == null || principal.type() != AuthPrincipal.PrincipalType.ADMIN) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Admin access required");
        }
        return adminUserRepository.findById(principal.id())
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Unknown admin account"));
    }
}