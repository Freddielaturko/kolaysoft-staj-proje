package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Ad soyad bos birakilamaz")
        String adSoyad,

        @NotBlank(message = "Email bos birakilamaz")
        @Email(message = "Gecerli bir email giriniz")
        String email,

        @NotBlank(message = "Sifre bos birakilamaz")
        @Size(min = 6, message = "Sifre en az 6 karakter olmalidir")
        String sifre,

        @NotNull(message = "Rol secilmelidir")
        Role rol
) {
}
