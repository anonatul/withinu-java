package com.withinu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.withinu.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        JwtService.ParsedToken parsed = jwtService.parse(token);

        if (!parsed.valid()) {
            ErrorCode code = parsed.expired() ? ErrorCode.TOKEN_EXPIRED : ErrorCode.INVALID_TOKEN;
            String message = parsed.expired() ? "Token has expired" : "Invalid token";
            writeError(response, code, message);
            return;
        }

        String role = parsed.type() == AuthPrincipal.PrincipalType.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
        AuthPrincipal principal = new AuthPrincipal(parsed.type(), parsed.id());
        var authentication = new UsernamePasswordAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
            "success", false,
            "errorCode", code.name(),
            "message", message
        ));
    }
}