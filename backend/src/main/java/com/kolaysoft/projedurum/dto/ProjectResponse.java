package com.kolaysoft.projedurum.dto;

import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
        Long id,
        String kod,
        String ad,
        String aciklama,
        String musteri,
        String departman,
        Long butce,
        LocalDate teslimTarihi,
        Long sorumluPmId,
        String sorumluPmAdSoyad,
        Long ekipLideriId,
        String ekipLideriAdSoyad,
        String durum,
        String saglik,
        Integer ilerleme,
        LocalDate sonRaporHaftasi,
        boolean raporGecikti,
        List<String> riskler
) {
}
