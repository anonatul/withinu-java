package com.withinu.security;

import java.util.UUID;

public record AuthPrincipal(PrincipalType type, UUID id) {

    public enum PrincipalType {
        USER,
        ADMIN
    }
}