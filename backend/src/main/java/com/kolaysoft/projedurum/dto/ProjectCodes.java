package com.kolaysoft.projedurum.dto;

import com.kolaysoft.projedurum.entity.Project;

// Kodu girilmemis eski projeler icin id'den sabit bir kod uretiyorum (KLF-007 gibi)
public final class ProjectCodes {

    private ProjectCodes() {
    }

    public static String of(Project project) {
        if (project.getKod() != null && !project.getKod().isBlank()) {
            return project.getKod();
        }
        return String.format("KLF-%03d", project.getId());
    }
}
