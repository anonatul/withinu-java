package com.withinu.controller;

import com.withinu.dto.GeoTokenRequest;
import com.withinu.dto.GeoTokenResponse;
import com.withinu.security.AuthPrincipal;
import com.withinu.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Geo Token", description = "Anonymous identity issuance after campus geo-verification")
@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class GeoTokenController {

    private final TokenService tokenService;

    @Operation(summary = "Verify campus presence and issue an anonymous JWT")
    @PostMapping("/geo")
    public GeoTokenResponse issueGeoToken(@Valid @RequestBody GeoTokenRequest request,
                                          @AuthenticationPrincipal AuthPrincipal principal) {
        return tokenService.issueGeoToken(request, principal);
    }
}