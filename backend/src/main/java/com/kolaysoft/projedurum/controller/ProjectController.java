package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.ProjectRequest;
import com.kolaysoft.projedurum.dto.ProjectResponse;
import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.enums.Role;
import com.kolaysoft.projedurum.repository.ProjectRepository;
import com.kolaysoft.projedurum.repository.UserRepository;
import com.kolaysoft.projedurum.security.ProjectAccessGuard;
import com.kolaysoft.projedurum.security.UserPrincipal;
import com.kolaysoft.projedurum.service.ProjectInsightService;
import com.kolaysoft.projedurum.service.ProjectLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessGuard accessGuard;
    private final ProjectLookupService projectLookupService;
    private final ProjectInsightService insightService;

    // Sadece ADMIN: proje olusturma ve guncelleme (SecurityConfig'te /api/admin/** -> ADMIN)
    @PostMapping("/api/admin/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest request) {
        Project project = new Project();
        apply(project, request);
        return insightService.toResponse(projectRepository.save(project));
    }

    @PutMapping("/api/admin/projects/{id}")
    public ProjectResponse updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        Project project = projectLookupService.findOr404(id);
        apply(project, request);
        return insightService.toResponse(projectRepository.save(project));
    }

    // PM ve Ekip Lideri: sadece kendi projeleri | CTO ve Admin: tum projeler
    @GetMapping("/api/projects")
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal UserPrincipal principal) {
        return insightService.toResponses(insightService.accessibleProjects(principal));
    }

    @GetMapping("/api/projects/{id}")
    public ProjectResponse getProject(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectLookupService.findOr404(id);
        accessGuard.ensureCanAccess(principal, project);
        return insightService.toResponse(project);
    }

    private void apply(Project project, ProjectRequest request) {
        String kod = blankToNull(request.kod());
        if (kod != null) {
            kod = kod.toUpperCase();
            if (!kod.equals(project.getKod()) && projectRepository.existsByKod(kod)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu proje kodu baska bir projede kullaniliyor");
            }
        }

        project.setKod(kod);
        project.setAd(request.ad());
        project.setAciklama(blankToNull(request.aciklama()));
        project.setMusteri(blankToNull(request.musteri()));
        project.setDepartman(blankToNull(request.departman()));
        project.setButce(request.butce());
        project.setTeslimTarihi(request.teslimTarihi());
        project.setSorumluPm(findWithRole(request.sorumluPmId(), Role.PM, "Sorumlu PM"));
        project.setEkipLideri(request.ekipLideriId() == null
                ? null
                : findWithRole(request.ekipLideriId(), Role.LEAD, "Ekip lideri"));
        project.setDurum(request.durum());
    }

    private User findWithRole(Long userId, Role role, String label) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, label + " bulunamadi"));
        if (user.getRol() != role) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " olarak secilen kullanicinin rolu uygun degil");
        }
        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
