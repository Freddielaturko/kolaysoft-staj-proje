package com.kolaysoft.projedurum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email bos birakilamaz")
        @Email(message = "Gecerli bir email giriniz")
        String email,

        @NotBlank(message = "Sifre bos birakilamaz")
        String sifre
) {
}
