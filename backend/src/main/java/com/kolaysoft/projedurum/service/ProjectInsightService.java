package com.kolaysoft.projedurum.service;

import com.kolaysoft.projedurum.dto.AlertResponse;
import com.kolaysoft.projedurum.dto.DashboardSummaryResponse;
import com.kolaysoft.projedurum.dto.DashboardSummaryResponse.TrendPoint;
import com.kolaysoft.projedurum.dto.ProjectCodes;
import com.kolaysoft.projedurum.dto.ProjectResponse;
import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.WeeklyReport;
import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.ProjectHealth;
import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import com.kolaysoft.projedurum.repository.ProjectRepository;
import com.kolaysoft.projedurum.repository.WeeklyReportRepository;
import com.kolaysoft.projedurum.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Projenin saglik durumunu, ilerlemesini ve uyarilarini son haftalik rapordan turetiyorum.
// Bu degerleri ayri kolon olarak tutmak yerine hesaplamayi sectim; boylece rapor girildigi
// anda dashboard da guncel oluyor ve iki kaynak arasinda tutarsizlik olusmuyor.
@Service
@RequiredArgsConstructor
public class ProjectInsightService {

    private static final int TREND_HAFTA = 12;
    private static final int TESLIM_UYARI_GUN = 14;

    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    @Value("${app.zone:Europe/Istanbul}")
    private String zone;

    public LocalDate today() {
        return LocalDate.now(ZoneId.of(zone));
    }

    public LocalDate currentWeek() {
        return today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public List<Project> accessibleProjects(UserPrincipal principal) {
        User user = principal.getUser();
        return switch (user.getRol()) {
            case PM -> projectRepository.findBySorumluPmId(user.getId());
            case LEAD -> projectRepository.findByEkipLideriId(user.getId());
            case CTO, ADMIN -> projectRepository.findAll();
        };
    }

    public List<ProjectResponse> toResponses(List<Project> projects) {
        Map<Long, WeeklyReport> latest = latestReports();
        return projects.stream()
                .sorted(Comparator.comparing(Project::getId))
                .map(p -> toResponse(p, latest.get(p.getId())))
                .toList();
    }

    public ProjectResponse toResponse(Project project) {
        WeeklyReport latest = weeklyReportRepository
                .findFirstByProjectIdOrderByRaporHaftasiDesc(project.getId())
                .orElse(null);
        return toResponse(project, latest);
    }

    public List<AlertResponse> alerts(List<Project> projects) {
        Map<Long, WeeklyReport> latest = latestReports();
        List<AlertResponse> alerts = new ArrayList<>();

        for (Project p : projects) {
            if (!isOngoing(p)) {
                continue;
            }
            WeeklyReport r = latest.get(p.getId());
            int ilerleme = progress(p, r);

            if (r != null && r.getGenelDurum() != GenelDurum.YESIL) {
                String seviye = r.getGenelDurum() == GenelDurum.KIRMIZI ? "KRITIK" : "DIKKAT";
                List<String> riskler = risks(r);
                if (riskler.isEmpty()) {
                    riskler = List.of(r.getGenelDurum() == GenelDurum.KIRMIZI
                            ? "Genel durum kırmızı olarak raporlandı"
                            : "Genel durum sarı olarak raporlandı");
                }
                for (int i = 0; i < riskler.size(); i++) {
                    alerts.add(alert(p, "risk-" + i, seviye, riskler.get(i)));
                }
            }

            if (p.getTeslimTarihi() != null && ilerleme < 100) {
                long kalan = ChronoUnit.DAYS.between(today(), p.getTeslimTarihi());
                if (kalan < 0) {
                    alerts.add(alert(p, "teslim", "KRITIK", "Teslim tarihi " + (-kalan) + " gün geçti"));
                } else if (isDeadlineClose(p, r)) {
                    alerts.add(alert(p, "teslim", "DIKKAT",
                            "Teslime " + kalan + " gün kaldı, ilerleme %" + ilerleme));
                }
            }

            if (isReportLate(p, r)) {
                alerts.add(alert(p, "rapor", "DIKKAT",
                        r == null ? "Henüz haftalık rapor girilmedi" : "Eksik haftalık rapor"));
            }
        }

        alerts.sort(Comparator.comparing((AlertResponse a) -> a.seviye().equals("KRITIK") ? 0 : 1));
        return alerts;
    }

    public DashboardSummaryResponse summary(List<Project> projects) {
        Map<Long, WeeklyReport> latest = latestReports();
        List<Project> aktif = projects.stream().filter(p -> p.getDurum() == ProjectStatus.AKTIF).toList();

        int ortalama = (int) Math.round(aktif.stream()
                .mapToInt(p -> progress(p, latest.get(p.getId())))
                .average().orElse(0));

        Map<ProjectHealth, Long> sagliklar = projects.stream()
                .collect(Collectors.groupingBy(p -> health(p, latest.get(p.getId())), Collectors.counting()));

        LocalDate buHafta = currentWeek();
        List<Long> aktifIds = aktif.stream().map(Project::getId).toList();
        List<WeeklyReport> aktifRaporlar = aktifIds.isEmpty()
                ? List.of()
                : weeklyReportRepository.findByProjectIdInOrderByRaporHaftasiDesc(aktifIds);

        int buHaftaRaporlanan = (int) aktifRaporlar.stream()
                .filter(r -> weekOf(r.getRaporHaftasi()).equals(buHafta))
                .map(r -> r.getProject().getId())
                .distinct()
                .count();

        // Her hafta icin aktif projelerin o haftaya kadarki son bilinen ilerlemesinin ortalamasi.
        // Sadece o hafta rapor girenlerin ortalamasini alsaydim, rapor giren projeler degistikce
        // grafik anlamsizca inip cikiyordu.
        Map<Long, List<WeeklyReport>> projeRaporlari = aktifRaporlar.stream()
                .collect(Collectors.groupingBy(r -> r.getProject().getId()));
        List<TrendPoint> trend = new ArrayList<>();
        for (int i = TREND_HAFTA - 1; i >= 0; i--) {
            LocalDate hafta = buHafta.minusWeeks(i);
            OptionalDouble deger = projeRaporlari.values().stream()
                    .map(raporlar -> raporlar.stream()
                            .filter(r -> !weekOf(r.getRaporHaftasi()).isAfter(hafta))
                            .findFirst())
                    .flatMap(Optional::stream)
                    .mapToInt(WeeklyReport::getGerceklesenIlerleme)
                    .average();
            trend.add(new TrendPoint(hafta, deger.isPresent() ? (int) Math.round(deger.getAsDouble()) : null));
        }

        return new DashboardSummaryResponse(
                aktif.size(),
                ortalama,
                weeklyChange(trend),
                sagliklar.getOrDefault(ProjectHealth.KRITIK, 0L).intValue(),
                sagliklar.getOrDefault(ProjectHealth.DIKKAT, 0L).intValue(),
                buHaftaRaporlanan,
                aktif.size(),
                buHafta,
                trend
        );
    }

    private ProjectResponse toResponse(Project p, WeeklyReport latest) {
        User lead = p.getEkipLideri();
        return new ProjectResponse(
                p.getId(),
                ProjectCodes.of(p),
                p.getAd(),
                p.getAciklama(),
                p.getMusteri(),
                p.getDepartman(),
                p.getButce(),
                p.getTeslimTarihi(),
                p.getSorumluPm().getId(),
                p.getSorumluPm().getAdSoyad(),
                lead == null ? null : lead.getId(),
                lead == null ? null : lead.getAdSoyad(),
                p.getDurum().name(),
                health(p, latest).name(),
                progress(p, latest),
                latest == null ? null : latest.getRaporHaftasi(),
                isReportLate(p, latest),
                latest == null ? List.of() : risks(latest)
        );
    }

    private ProjectHealth health(Project p, WeeklyReport latest) {
        if (p.getDurum() == ProjectStatus.ARSIV || p.getDurum() == ProjectStatus.IPTAL) {
            return ProjectHealth.ARSIV;
        }
        if (p.getDurum() == ProjectStatus.TAMAMLANDI) {
            return ProjectHealth.NORMAL;
        }
        boolean teslimGecti = p.getTeslimTarihi() != null
                && p.getTeslimTarihi().isBefore(today())
                && progress(p, latest) < 100;
        if (teslimGecti || (latest != null && latest.getGenelDurum() == GenelDurum.KIRMIZI)) {
            return ProjectHealth.KRITIK;
        }
        if (latest == null || latest.getGenelDurum() == GenelDurum.SARI || isReportLate(p, latest)
                || isDeadlineClose(p, latest)) {
            return ProjectHealth.DIKKAT;
        }
        return ProjectHealth.NORMAL;
    }

    private int progress(Project p, WeeklyReport latest) {
        if (latest != null) {
            return latest.getGerceklesenIlerleme();
        }
        return p.getDurum() == ProjectStatus.TAMAMLANDI ? 100 : 0;
    }

    // Gecen haftanin raporu da yoksa rapor gecikmis sayiyorum; bu haftanin raporu hafta icinde girilebilir
    private boolean isReportLate(Project p, WeeklyReport latest) {
        if (p.getDurum() != ProjectStatus.AKTIF) {
            return false;
        }
        LocalDate gecenHafta = currentWeek().minusWeeks(1);
        return latest == null || weekOf(latest.getRaporHaftasi()).isBefore(gecenHafta);
    }

    // Teslime iki haftadan az kalmis ve ilerleme %80'in altindaysa
    private boolean isDeadlineClose(Project p, WeeklyReport latest) {
        if (p.getTeslimTarihi() == null) {
            return false;
        }
        long kalan = ChronoUnit.DAYS.between(today(), p.getTeslimTarihi());
        return kalan >= 0 && kalan <= TESLIM_UYARI_GUN && progress(p, latest) < 80;
    }

    private boolean isOngoing(Project p) {
        return p.getDurum() == ProjectStatus.AKTIF || p.getDurum() == ProjectStatus.BEKLEMEDE;
    }

    private List<String> risks(WeeklyReport r) {
        if (r.getRiskEngelNotu() == null) {
            return List.of();
        }
        return Arrays.stream(r.getRiskEngelNotu().split("\\R"))
                .map(String::strip)
                .map(s -> s.startsWith("- ") ? s.substring(2) : s)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private Integer weeklyChange(List<TrendPoint> trend) {
        List<Integer> dolu = trend.stream().map(TrendPoint::ortalamaIlerleme).filter(Objects::nonNull).toList();
        if (dolu.size() < 2) {
            return null;
        }
        return dolu.get(dolu.size() - 1) - dolu.get(dolu.size() - 2);
    }

    private Map<Long, WeeklyReport> latestReports() {
        return weeklyReportRepository.findLatestReportPerProject().stream()
                .collect(Collectors.toMap(r -> r.getProject().getId(), Function.identity(), (a, b) -> a));
    }

    private LocalDate weekOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private AlertResponse alert(Project p, String key, String seviye, String mesaj) {
        return new AlertResponse(p.getId() + "-" + key, p.getId(), ProjectCodes.of(p), p.getAd(), seviye, mesaj);
    }
}
