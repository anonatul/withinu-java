package com.withinu.dto;

public record GeoTokenResponse(boolean success, String token, long expiresIn) {
}