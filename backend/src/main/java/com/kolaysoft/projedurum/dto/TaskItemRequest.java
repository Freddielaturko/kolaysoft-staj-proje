package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskItemRequest(
        @NotBlank(message = "Baslik bos birakilamaz")
        String baslik,

        String aciklama,
        String sorumlu,

        @NotNull(message = "Durum secilmelidir")
        TaskStatus durum,

        LocalDate planlananTarih,
        LocalDate tamamlananTarih,
        String not
) {
}
