package com.kolaysoft.projedurum.dto;

public record LoginResponse(
        String token,
        Long userId,
        String adSoyad,
        String email,
        String rol
) {
}
