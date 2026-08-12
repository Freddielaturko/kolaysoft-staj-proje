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
import com.kolaysoft.projedurum.service.ProjectLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessGuard accessGuard;
    private final ProjectLookupService projectLookupService;

    // Sadece ADMIN: proje olusturma (SecurityConfig'te /api/admin/** -> ADMIN)
    @PostMapping("/api/admin/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest request) {
        User pm = userRepository.findById(request.sorumluPmId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sorumlu PM bulunamadi"));

        if (pm.getRol() != Role.PM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secilen kullanici PM rolunde degil");
        }

        Project project = new Project();
        project.setAd(request.ad());
        project.setMusteri(request.musteri());
        project.setSorumluPm(pm);
        project.setDurum(request.durum());

        return ProjectResponse.from(projectRepository.save(project));
    }

    // PM: sadece kendi projeleri | CTO: tum projeler
    @GetMapping("/api/projects")
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal UserPrincipal principal) {
        Role rol = principal.getUser().getRol();

        List<Project> projects = (rol == Role.PM)
                ? projectRepository.findBySorumluPmId(principal.getId())
                : projectRepository.findAll();

        return projects.stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/api/projects/{id}")
    public ProjectResponse getProject(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectLookupService.findOr404(id);
        accessGuard.ensureCanAccess(principal, project);
        return ProjectResponse.from(project);
    }
}
