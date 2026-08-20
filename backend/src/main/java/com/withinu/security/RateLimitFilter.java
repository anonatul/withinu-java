package com.withinu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.withinu.exception.ApiException;
import com.withinu.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            if (path.startsWith("/api/v1") && !path.equals("/api/v1/health")) {
                rateLimitService.checkIpRateLimit(clientIp(request));

                if ("/api/v1/messages".equals(path) && "POST".equalsIgnoreCase(request.getMethod())) {
                    var authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal
                        && principal.type() == AuthPrincipal.PrincipalType.USER) {
                        rateLimitService.checkUserMessageRateLimit(principal.id());
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (ApiException e) {
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                "success", false,
                "errorCode", e.getErrorCode().name(),
                "message", e.getMessage()
            ));
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return ip == null ? "unknown" : ip;
    }
}