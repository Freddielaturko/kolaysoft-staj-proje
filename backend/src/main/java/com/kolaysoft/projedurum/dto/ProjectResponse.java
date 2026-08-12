package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.Project;

public record ProjectResponse(
        Long id,
        String ad,
        String musteri,
        Long sorumluPmId,
        String sorumluPmAdSoyad,
        String durum
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getAd(),
                project.getMusteri(),
                project.getSorumluPm().getId(),
                project.getSorumluPm().getAdSoyad(),
                project.getDurum().name()
        );
    }
}
