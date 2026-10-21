package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.WeeklyReport;

import java.time.LocalDate;

public record WeeklyReportResponse(
        Long id,
        Long projectId,
        String projectKod,
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
        String butceDurumu,
        String ekipDurumu,
        Integer revizyon,
        String olusturanUserAdSoyad
) {
    public static WeeklyReportResponse from(WeeklyReport r) {
        return new WeeklyReportResponse(
                r.getId(),
                r.getProject().getId(),
                ProjectCodes.of(r.getProject()),
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
                r.getButceDurumu(),
                r.getEkipDurumu(),
                r.getRevizyon() == null ? 1 : r.getRevizyon(),
                r.getOlusturanUser().getAdSoyad()
        );
    }
}
