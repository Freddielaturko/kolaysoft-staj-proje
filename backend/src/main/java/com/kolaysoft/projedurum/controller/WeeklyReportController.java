package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.WeeklyReportRequest;
import com.kolaysoft.projedurum.dto.WeeklyReportResponse;
import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.WeeklyReport;
import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.RiskLevel;
import com.kolaysoft.projedurum.repository.WeeklyReportRepository;
import com.kolaysoft.projedurum.security.ProjectAccessGuard;
import com.kolaysoft.projedurum.security.UserPrincipal;
import com.kolaysoft.projedurum.service.ProjectLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportRepository weeklyReportRepository;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessGuard accessGuard;

    // PM: kendi projesi icin haftalik rapor olusturur/gunceller (ayni proje+hafta -> upsert)
    @PostMapping("/api/projects/{projectId}/weekly-reports")
    public WeeklyReportResponse createOrUpdateReport(
            @PathVariable Long projectId,
            @Valid @RequestBody WeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Project project = projectLookupService.findOr404(projectId);
        accessGuard.ensureCanWrite(principal, project);

        WeeklyReport report = weeklyReportRepository
                .findByProjectIdAndRaporHaftasi(projectId, request.raporHaftasi())
                .orElseGet(WeeklyReport::new);

        report.setProject(project);
        report.setRaporHaftasi(request.raporHaftasi());
        report.setHedeflenenIlerleme(request.hedeflenenIlerleme());
        report.setGerceklesenIlerleme(request.gerceklesenIlerleme());
        report.setGenelDurum(request.genelDurum());
        report.setRiskSeviyesi(request.riskSeviyesi());
        report.setCanliTask(request.canliTask());
        report.setYapilanlar(request.yapilanlar());
        report.setYapilacaklar(request.yapilacaklar());
        report.setRiskEngelNotu(request.riskEngelNotu());
        report.setGenelNot(request.genelNot());
        report.setOlusturanUser(principal.getUser());

        return WeeklyReportResponse.from(weeklyReportRepository.save(report));
    }

    // PM: kendi projesinin gecmis raporlari | CTO: herhangi bir projenin gecmis raporlari
    @GetMapping("/api/projects/{projectId}/weekly-reports")
    public List<WeeklyReportResponse> listReportsForProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Project project = projectLookupService.findOr404(projectId);
        accessGuard.ensureCanAccess(principal, project);

        return weeklyReportRepository.findByProjectIdOrderByRaporHaftasiDesc(projectId)
                .stream().map(WeeklyReportResponse::from).toList();
    }

    // CTO dashboard: her projenin en son raporu, opsiyonel filtrelerle (proje/hafta/durum/risk)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CTO')")
    @GetMapping("/api/dashboard/cto")
    public List<WeeklyReportResponse> ctoDashboard(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) LocalDate raporHaftasi,
            @RequestParam(required = false) GenelDurum genelDurum,
            @RequestParam(required = false) RiskLevel riskSeviyesi
    ) {
        return weeklyReportRepository.findLatestReportPerProject().stream()
                .filter(r -> projectId == null || r.getProject().getId().equals(projectId))
                .filter(r -> raporHaftasi == null || r.getRaporHaftasi().equals(raporHaftasi))
                .filter(r -> genelDurum == null || r.getGenelDurum() == genelDurum)
                .filter(r -> riskSeviyesi == null || r.getRiskSeviyesi() == riskSeviyesi)
                .map(WeeklyReportResponse::from)
                .toList();
    }
}
