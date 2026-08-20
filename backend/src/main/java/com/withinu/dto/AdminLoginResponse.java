package com.withinu.dto;

public record AdminLoginResponse(boolean success, String token, long expiresIn, String username) {
}