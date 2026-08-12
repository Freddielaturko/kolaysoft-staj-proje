package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.RiskLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WeeklyReportRequest(
        @NotNull(message = "Rapor haftasi bos birakilamaz")
        LocalDate raporHaftasi,

        @NotNull(message = "Hedeflenen ilerleme bos birakilamaz")
        @Min(value = 0, message = "Hedeflenen ilerleme 0-100 arasinda olmalidir")
        @Max(value = 100, message = "Hedeflenen ilerleme 0-100 arasinda olmalidir")
        Integer hedeflenenIlerleme,

        @NotNull(message = "Gerceklesen ilerleme bos birakilamaz")
        @Min(value = 0, message = "Gerceklesen ilerleme 0-100 arasinda olmalidir")
        @Max(value = 100, message = "Gerceklesen ilerleme 0-100 arasinda olmalidir")
        Integer gerceklesenIlerleme,

        @NotNull(message = "Genel durum secilmelidir")
        GenelDurum genelDurum,

        @NotNull(message = "Risk seviyesi secilmelidir")
        RiskLevel riskSeviyesi,

        String canliTask,
        String yapilanlar,
        String yapilacaklar,
        String riskEngelNotu,
        String genelNot
) {
}
