package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.TaskItem;

import java.time.LocalDate;

public record TaskItemResponse(
        Long id,
        Long projectId,
        String baslik,
        String aciklama,
        String sorumlu,
        String durum,
        LocalDate planlananTarih,
        LocalDate tamamlananTarih,
        String not
) {
    public static TaskItemResponse from(TaskItem t) {
        return new TaskItemResponse(
                t.getId(),
                t.getProject().getId(),
                t.getBaslik(),
                t.getAciklama(),
                t.getSorumlu(),
                t.getDurum().name(),
                t.getPlanlananTarih(),
                t.getTamamlananTarih(),
                t.getNot()
        );
    }
}
