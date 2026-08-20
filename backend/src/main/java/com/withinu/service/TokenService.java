package com.withinu.service;

import com.withinu.dto.GeoTokenRequest;
import com.withinu.dto.GeoTokenResponse;
import com.withinu.entity.AnonymousUser;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import com.withinu.repository.AnonymousUserRepository;
import com.withinu.security.AuthPrincipal;
import com.withinu.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final GeoVerificationService geoVerificationService;
    private final AnonymousIdentityService identityService;
    private final AnonymousUserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public GeoTokenResponse issueGeoToken(GeoTokenRequest request, AuthPrincipal existingPrincipal) {
        if (!geoVerificationService.isInsideCampus(request.latitude(), request.longitude())) {
            throw new ApiException(ErrorCode.OUTSIDE_CAMPUS,
                "You must be inside the campus to access WithinU");
        }

        AnonymousUser user;
        if (existingPrincipal != null && existingPrincipal.type() == AuthPrincipal.PrincipalType.USER) {
            user = userRepository.findById(existingPrincipal.id())
                .orElseGet(identityService::createIdentity);
            identityService.reVerify(user);
        } else {
            user = identityService.createIdentity();
        }

        String token = jwtService.issueUserToken(user.getId(), user.getTokenVersion());
        return new GeoTokenResponse(true, token, jwtService.userTokenTtlSeconds());
    }
}