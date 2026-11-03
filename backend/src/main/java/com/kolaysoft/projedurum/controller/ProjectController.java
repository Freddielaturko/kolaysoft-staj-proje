package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.ProjectRequest;
import com.kolaysoft.projedurum.dto.ProjectResponse;
import com.kolaysoft.projedurum.dto.UserResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Proje olusturma ve guncelleme Admin ve PM icin acik.
// Admin herhangi bir PM'i sorumlu atayabilir; PM'in actigi projede sorumlu her zaman kendisi oluyor
// ve PM sadece kendi projelerini duzenleyebiliyor.
@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessGuard accessGuard;
    private final ProjectLookupService projectLookupService;
    private final ProjectInsightService insightService;

    @PostMapping("/api/projects")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectRequest request,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        Project project = new Project();
        apply(project, request, principal);
        return insightService.toResponse(projectRepository.save(project));
    }

    @PutMapping("/api/projects/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody ProjectRequest request,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectLookupService.findOr404(id);
        accessGuard.ensureCanWrite(principal, project);
        apply(project, request, principal);
        return insightService.toResponse(projectRepository.save(project));
    }

    // Eski istemciler icin birakildi; /api/admin/** SecurityConfig'te zaten ADMIN ile sinirli
    @PostMapping("/api/admin/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProjectAsAdmin(@Valid @RequestBody ProjectRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return createProject(request, principal);
    }

    @PutMapping("/api/admin/projects/{id}")
    public ProjectResponse updateProjectAsAdmin(@PathVariable Long id,
                                                @Valid @RequestBody ProjectRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return updateProject(id, request, principal);
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

    // Proje formundaki secim listeleri icin. PM'in tum kullanici listesini gormesine gerek yok,
    // sadece atayabilecegi rolleri donuyorum.
    @GetMapping("/api/users/assignable")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    public List<UserResponse> assignableUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRol() == Role.PM || u.getRol() == Role.LEAD)
                .sorted(Comparator.comparing(User::getAdSoyad))
                .map(UserResponse::from)
                .toList();
    }

    private void apply(Project project, ProjectRequest request, UserPrincipal principal) {
        String kod = blankToNull(request.kod());
        if (kod != null) {
            kod = kod.toUpperCase();
            if (!kod.equals(project.getKod()) && projectRepository.existsByKod(kod)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu proje kodu baska bir projede kullaniliyor");
            }
        }

        User pm;
        if (principal.getUser().getRol() == Role.PM) {
            pm = principal.getUser();
        } else {
            if (request.sorumluPmId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sorumlu proje yoneticisi secilmelidir");
            }
            pm = findWithRole(request.sorumluPmId(), Role.PM, "Sorumlu PM");
        }

        project.setKod(kod);
        project.setAd(request.ad().strip());
        project.setAciklama(blankToNull(request.aciklama()));
        project.setMusteri(blankToNull(request.musteri()));
        project.setDepartman(blankToNull(request.departman()));
        project.setButce(request.butce());
        project.setTeslimTarihi(request.teslimTarihi());
        project.setSorumluPm(pm);
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
