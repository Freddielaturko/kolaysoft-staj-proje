package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @Size(max = 20, message = "Proje kodu en fazla 20 karakter olabilir")
        String kod,

        @NotBlank(message = "Proje adi bos birakilamaz")
        String ad,

        String aciklama,

        String musteri,

        String departman,

        @Min(value = 0, message = "Butce negatif olamaz")
        Long butce,

        LocalDate teslimTarihi,

        @NotNull(message = "Sorumlu proje yoneticisi secilmelidir")
        Long sorumluPmId,

        Long ekipLideriId,

        @NotNull(message = "Durum secilmelidir")
        ProjectStatus durum
) {
}
