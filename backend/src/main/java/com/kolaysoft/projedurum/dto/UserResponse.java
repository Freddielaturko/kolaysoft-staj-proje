package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.User;

public record UserResponse(
        Long id,
        String adSoyad,
        String email,
        String rol
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getAdSoyad(), user.getEmail(), user.getRol().name());
    }
}
