package com.kolaysoft.projedurum.security;

import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

// CTO ve Admin her projeyi gorebilir.
// PM yalnizca sorumlusu oldugu projeye, Ekip Lideri yalnizca lideri oldugu projeye erisebilir/yazabilir.
// CTO salt okunur; yazma islemi yapamaz.
@Component
public class ProjectAccessGuard {

    public boolean canAccess(UserPrincipal principal, Project project) {
        Role rol = principal.getUser().getRol();
        return switch (rol) {
            case PM -> isUser(project.getSorumluPm(), principal);
            case LEAD -> isUser(project.getEkipLideri(), principal);
            case CTO, ADMIN -> true;
        };
    }

    public void ensureCanAccess(UserPrincipal principal, Project project) {
        if (!canAccess(principal, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu projeye erisim yetkiniz yok");
        }
    }

    public void ensureCanWrite(UserPrincipal principal, Project project) {
        if (principal.getUser().getRol() == Role.CTO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CTO rolu yazma islemi yapamaz");
        }
        if (!canAccess(principal, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu projeye yazma yetkiniz yok");
        }
    }

    private boolean isUser(User user, UserPrincipal principal) {
        return user != null && user.getId().equals(principal.getId());
    }
}
