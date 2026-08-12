package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequest(
        @NotBlank(message = "Proje adi bos birakilamaz")
        String ad,

        String musteri,

        @NotNull(message = "Sorumlu proje yoneticisi secilmelidir")
        Long sorumluPmId,

        @NotNull(message = "Durum secilmelidir")
        ProjectStatus durum
) {
}
