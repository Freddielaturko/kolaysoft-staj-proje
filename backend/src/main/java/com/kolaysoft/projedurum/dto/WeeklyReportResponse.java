package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.WeeklyReport;

import java.time.LocalDate;

public record WeeklyReportResponse(
        Long id,
        Long projectId,
        String projectAd,
        LocalDate raporHaftasi,
        Integer hedeflenenIlerleme,
        Integer gerceklesenIlerleme,
        String genelDurum,
        String riskSeviyesi,
        String canliTask,
        String yapilanlar,
        String yapilacaklar,
        String riskEngelNotu,
        String genelNot,
        String olusturanUserAdSoyad
) {
    public static WeeklyReportResponse from(WeeklyReport r) {
        return new WeeklyReportResponse(
                r.getId(),
                r.getProject().getId(),
                r.getProject().getAd(),
                r.getRaporHaftasi(),
                r.getHedeflenenIlerleme(),
                r.getGerceklesenIlerleme(),
                r.getGenelDurum().name(),
                r.getRiskSeviyesi().name(),
                r.getCanliTask(),
                r.getYapilanlar(),
                r.getYapilacaklar(),
                r.getRiskEngelNotu(),
                r.getGenelNot(),
                r.getOlusturanUser().getAdSoyad()
        );
    }
}
