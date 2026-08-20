package com.withinu.service;

import com.withinu.config.WithinuProperties;
import com.withinu.entity.AnonymousUser;
import com.withinu.repository.AnonymousUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnonymousIdentityService {

    private final AnonymousUserRepository userRepository;
    private final WithinuProperties props;

    @Transactional
    public AnonymousUser createIdentity() {
        Instant now = Instant.now();
        AnonymousUser user = AnonymousUser.builder()
            .tokenVersion(0)
            .createdAt(now)
            .expiresAt(now.plus(props.jwt().userTokenTtl()))
            .lastVerifiedAt(now)
            .build();
        return userRepository.save(user);
    }

    @Transactional
    public AnonymousUser reVerify(AnonymousUser user) {
        Instant now = Instant.now();
        user.setLastVerifiedAt(now);
        user.setExpiresAt(now.plus(props.jwt().userTokenTtl()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return userRepository.save(user);
    }
}