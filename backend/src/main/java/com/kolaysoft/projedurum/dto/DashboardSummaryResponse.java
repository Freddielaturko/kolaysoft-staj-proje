package com.kolaysoft.projedurum.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
        int aktifProje,
        int ortalamaIlerleme,
        Integer ortalamaIlerlemeDegisim,
        int kritikProje,
        int dikkatProje,
        int buHaftaRaporlanan,
        int buHaftaBeklenen,
        LocalDate buHafta,
        List<TrendPoint> trend
) {
    public record TrendPoint(LocalDate hafta, Integer ortalamaIlerleme) {
    }
}
