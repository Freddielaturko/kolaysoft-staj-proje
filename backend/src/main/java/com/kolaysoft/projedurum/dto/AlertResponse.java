package com.kolaysoft.projedurum.dto;

public record AlertResponse(
        String id,
        Long projectId,
        String projectKod,
        String projectAd,
        String seviye,
        String mesaj
) {
}
