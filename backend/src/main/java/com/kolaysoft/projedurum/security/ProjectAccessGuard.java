package com.kolaysoft.projedurum.security;

import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// CTO her projeyi gorebilir. PM yalnizca sorumlusu oldugu projeye erisebilir/yazabilir.
// Admin bu kontrolun disinda; admin endpoint'leri zaten SecurityConfig'te ayri sinirlandirildi.
@Component
public class ProjectAccessGuard {

    public void ensureCanAccess(UserPrincipal principal, Project project) {
        if (principal.getUser().getRol() == Role.PM
                && !project.getSorumluPm().getId().equals(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu projeye erisim yetkiniz yok");
        }
    }

    public void ensureCanWrite(UserPrincipal principal, Project project) {
        Role rol = principal.getUser().getRol();

        if (rol == Role.CTO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CTO rolu yazma islemi yapamaz");
        }

        if (rol == Role.PM && !project.getSorumluPm().getId().equals(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu projeye yazma yetkiniz yok");
        }
    }
}
