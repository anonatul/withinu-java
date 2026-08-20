package com.withinu.security;

import com.withinu.config.WithinuProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_VERSION = "ver";

    private final SecretKey key;
    private final WithinuProperties props;

    public JwtService(WithinuProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueUserToken(UUID anonymousUserId, int tokenVersion) {
        return issue(anonymousUserId, AuthPrincipal.PrincipalType.USER, tokenVersion,
            props.jwt().userTokenTtl());
    }

    public String issueAdminToken(UUID adminId) {
        return issue(adminId, AuthPrincipal.PrincipalType.ADMIN, 0, props.jwt().adminTokenTtl());
    }

    public long userTokenTtlSeconds() {
        return props.jwt().userTokenTtl().toSeconds();
    }

    public long adminTokenTtlSeconds() {
        return props.jwt().adminTokenTtl().toSeconds();
    }

    private String issue(UUID subject, AuthPrincipal.PrincipalType type, int version, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject.toString())
            .claim(CLAIM_TYPE, type.name())
            .claim(CLAIM_VERSION, version)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(ttl)))
            .signWith(key)
            .compact();
    }

    public ParsedToken parse(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return ParsedToken.of(claims);
        } catch (ExpiredJwtException e) {
            return ParsedToken.expired(e.getClaims());
        } catch (JwtException | IllegalArgumentException e) {
            return ParsedToken.invalid();
        }
    }

    public record ParsedToken(AuthPrincipal.PrincipalType type, UUID id, int version,
                              Instant expiresAt, boolean valid, boolean expired) {

        static ParsedToken of(Claims claims) {
            return new ParsedToken(
                AuthPrincipal.PrincipalType.valueOf(claims.get(CLAIM_TYPE, String.class)),
                UUID.fromString(claims.getSubject()),
                claims.get(CLAIM_VERSION, Integer.class) == null ? 0 : claims.get(CLAIM_VERSION, Integer.class),
                claims.getExpiration().toInstant(),
                true,
                false
            );
        }

        static ParsedToken expired(Claims claims) {
            try {
                return new ParsedToken(
                    AuthPrincipal.PrincipalType.valueOf(claims.get(CLAIM_TYPE, String.class)),
                    UUID.fromString(claims.getSubject()),
                    claims.get(CLAIM_VERSION, Integer.class) == null ? 0 : claims.get(CLAIM_VERSION, Integer.class),
                    claims.getExpiration().toInstant(),
                    false,
                    true
                );
            } catch (Exception e) {
                return invalid();
            }
        }

        static ParsedToken invalid() {
            return new ParsedToken(null, null, 0, null, false, false);
        }
    }
}