package com.kolaysoft.projedurum.config;

import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.WeeklyReport;
import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import com.kolaysoft.projedurum.entity.enums.RiskLevel;
import com.kolaysoft.projedurum.entity.enums.Role;
import com.kolaysoft.projedurum.repository.ProjectRepository;
import com.kolaysoft.projedurum.repository.UserRepository;
import com.kolaysoft.projedurum.repository.WeeklyReportRepository;
import com.kolaysoft.projedurum.service.ProjectInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// APP_DEMO_ENABLED=true iken ve hic proje yokken calisir. Canli demoda bos bir ekran yerine
// gercekci bir portfoy gorunsun diye ekledim. Tarihler bugune gore hesaplandigi icin
// demo ne zaman acilirsa acilsin raporlar, gecikmeler ve teslim uyarilari guncel kalir.
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectInsightService insightService;

    @Value("${app.demo.enabled:false}")
    private boolean enabled;

    @Value("${app.demo.password:demo123}")
    private String password;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || projectRepository.count() > 0) {
            return;
        }

        user("admin@kolaysoft.demo", "Sistem Admin", Role.ADMIN);
        user("cto@kolaysoft.demo", "Salih Mert Anarol", Role.CTO);
        User zeynep = user("pm@kolaysoft.demo", "Zeynep Demir", Role.PM);
        User mert = user("pm2@kolaysoft.demo", "Mert Aydın", Role.PM);
        User ece = user("lead@kolaysoft.demo", "Ece Özkan", Role.LEAD);
        User canan = user("lead2@kolaysoft.demo", "Canan Kaya", Role.LEAD);

        LocalDate today = insightService.today();
        LocalDate week = insightService.currentWeek();

        // Kritik: kirmizi raporlanmis ve gecen haftanin raporu eksik
        Project finans = project("KLF-241", "Finansal Raporlama Platformu",
                "Finans ekipleri için yeni nesil raporlama ve konsolidasyon platformu.",
                "Ürün & Teknoloji", 480_000L, today.plusDays(17), zeynep, ece, ProjectStatus.AKTIF);
        report(finans, ece, week.minusWeeks(7), 30, 30, GenelDurum.YESIL, RiskLevel.DUSUK,
                "Kimlik doğrulama altyapısı ve temel veri modeli.", "Banka API bağlantılarına başlanacak.",
                null, "Plan dahilinde", "İyi", 1);
        report(finans, ece, week.minusWeeks(6), 40, 38, GenelDurum.YESIL, RiskLevel.DUSUK,
                "Hesap planı eşleştirme ekranı tamamlandı.", "Banka API bağlantıları.",
                null, "Plan dahilinde", "İyi", 1);
        report(finans, ece, week.minusWeeks(5), 48, 44, GenelDurum.SARI, RiskLevel.ORTA,
                "İlk iki bankanın API bağlantısı kuruldu.", "Kalan bankalar ve hata yönetimi.",
                "Bir bankanın test ortamı erişimi gecikiyor", "Plan dahilinde", "İyi", 2);
        report(finans, ece, week.minusWeeks(4), 56, 51, GenelDurum.SARI, RiskLevel.ORTA,
                "Banka API bağlantıları için hata yönetimi tamamlandı.", "Mutabakat ekranı.",
                "Bir bankanın test ortamı erişimi gecikiyor", "Plan dahilinde", "Kapasite sınırda", 1);
        report(finans, ece, week.minusWeeks(3), 64, 57, GenelDurum.KIRMIZI, RiskLevel.YUKSEK,
                "Mutabakat ekranının ilk sürümü hazır.", "Performans testleri.",
                "API entegrasyonunda 4 gün gecikme\nMimari kurul onayı bekleniyor", "%8 sapma riski", "Kapasite kritik", 2);
        report(finans, ece, week.minusWeeks(2), 72, 62, GenelDurum.KIRMIZI, RiskLevel.YUKSEK,
                "Performans testlerinin ilk turu yapıldı.", "Darboğaz analizi ve iyileştirmeler.",
                "API entegrasyonunda 4 gün gecikme\nMimari kurul onayı bekleniyor", "%8 sapma riski", "Kapasite kritik", 1);

        Project portal = project("KLF-238", "Müşteri Deneyimi Portalı",
                "Müşteri taleplerini tek akışta yöneten self-servis portal.",
                "Müşteri Çözümleri", 295_000L, today.plusDays(41), zeynep, canan, ProjectStatus.AKTIF);
        int[] portalIlerleme = {22, 28, 33, 39, 44, 48};
        for (int i = 0; i < portalIlerleme.length; i++) {
            boolean son = i >= 4;
            report(portal, canan, week.minusWeeks(5 - i), portalIlerleme[i] + (son ? 6 : 2), portalIlerleme[i],
                    son ? GenelDurum.SARI : GenelDurum.YESIL, son ? RiskLevel.ORTA : RiskLevel.DUSUK,
                    PORTAL_YAPILAN[i], PORTAL_YAPILACAK[i],
                    son ? "İçerik onayı bekleniyor" : null, "Plan dahilinde", "İyi", 1);
        }

        Project ambar = project("KLF-229", "Veri Ambarı Modernizasyonu",
                "Eski veri ambarının ölçeklenebilir bulut mimarisine taşınması.",
                "Veri & Analitik", 720_000L, today.plusDays(66), mert, canan, ProjectStatus.AKTIF);
        int[] ambarIlerleme = {40, 45, 49, 54, 58, 62, 66, 69, 73, 76};
        for (int i = 0; i < ambarIlerleme.length; i++) {
            report(ambar, canan, week.minusWeeks(9 - i), ambarIlerleme[i], ambarIlerleme[i],
                    GenelDurum.YESIL, RiskLevel.DUSUK,
                    AMBAR_YAPILAN[i % AMBAR_YAPILAN.length], "Sonraki veri alanının taşınması.",
                    null, "Plan dahilinde", "İyi", 1);
        }

        // Yesil ama teslime az kaldi; teslim uyarisi uretir
        Project saha = project("KLF-246", "Mobil Saha Uygulaması",
                "Saha ekiplerinin iş emirlerini çevrimdışı takip ettiği mobil uygulama.",
                "Saha Operasyonları", 260_000L, today.plusDays(10), mert, ece, ProjectStatus.AKTIF);
        int[] sahaIlerleme = {41, 50, 57, 64};
        for (int i = 0; i < sahaIlerleme.length; i++) {
            report(saha, ece, week.minusWeeks(4 - i), sahaIlerleme[i] + 3, sahaIlerleme[i],
                    GenelDurum.YESIL, RiskLevel.ORTA,
                    SAHA_YAPILAN[i], "Çevrimdışı senkronizasyon testleri.",
                    null, "Plan dahilinde", "İyi", 1);
        }

        Project ik = project("KLF-217", "İK Self Servis", "Çalışan izin ve özlük işlemleri portalı.",
                "Kurumsal Uygulamalar", 180_000L, today.minusDays(6), mert, ece, ProjectStatus.TAMAMLANDI);
        report(ik, ece, week.minusWeeks(2), 92, 92, GenelDurum.YESIL, RiskLevel.DUSUK,
                "Kullanıcı kabul testleri tamamlandı.", "Canlıya geçiş.", null, "Plan dahilinde", "İyi", 1);
        report(ik, ece, week.minusWeeks(1), 100, 100, GenelDurum.YESIL, RiskLevel.DUSUK,
                "Canlıya geçiş yapıldı, eğitimler verildi.", "Proje kapanışı.", null, "Plan dahilinde", "İyi", 1);

        Project tedarik = project("KLF-205", "Tedarikçi Entegrasyon Merkezi",
                "Tedarikçi verilerinin merkezi entegrasyon katmanı.",
                "Operasyon", 340_000L, today.minusDays(120), zeynep, ece, ProjectStatus.ARSIV);
        report(tedarik, ece, week.minusWeeks(18), 100, 100, GenelDurum.YESIL, RiskLevel.DUSUK,
                "Son tedarikçi entegrasyonu tamamlandı.", "Proje kapanışı ve arşiv.", null, "Plan dahilinde", "İyi", 1);

        log.info("Demo verisi yuklendi: {} kullanici, {} proje", userRepository.count(), projectRepository.count());
    }

    private User user(String email, String adSoyad, Role rol) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setAdSoyad(adSoyad);
            u.setRol(rol);
            u.setSifreHash(passwordEncoder.encode(password));
            return userRepository.save(u);
        });
    }

    private Project project(String kod, String ad, String aciklama, String departman, Long butce,
                            LocalDate teslim, User pm, User lead, ProjectStatus durum) {
        Project p = new Project();
        p.setKod(kod);
        p.setAd(ad);
        p.setAciklama(aciklama);
        p.setMusteri("Kolaysoft");
        p.setDepartman(departman);
        p.setButce(butce);
        p.setTeslimTarihi(teslim);
        p.setSorumluPm(pm);
        p.setEkipLideri(lead);
        p.setDurum(durum);
        return projectRepository.save(p);
    }

    private void report(Project p, User author, LocalDate hafta, int hedef, int gercek, GenelDurum durum,
                        RiskLevel risk, String yapilan, String yapilacak, String riskNotu,
                        String butce, String ekip, int revizyon) {
        WeeklyReport r = new WeeklyReport();
        r.setProject(p);
        r.setOlusturanUser(author);
        r.setRaporHaftasi(hafta);
        r.setHedeflenenIlerleme(hedef);
        r.setGerceklesenIlerleme(gercek);
        r.setGenelDurum(durum);
        r.setRiskSeviyesi(risk);
        r.setYapilanlar(yapilan);
        r.setYapilacaklar(yapilacak);
        r.setRiskEngelNotu(riskNotu);
        r.setButceDurumu(butce);
        r.setEkipDurumu(ekip);
        r.setRevizyon(revizyon);
        reportRepository.save(r);
    }

    private static final String[] PORTAL_YAPILAN = {
            "Kullanıcı araştırması ve bilgi mimarisi.",
            "Tasarım sistemi ve temel bileşenler.",
            "Talep oluşturma akışı tamamlandı.",
            "Talep takibi ve bildirimler.",
            "Portal navigasyonu ve arama tamamlandı.",
            "Bilgi bankası şablonları hazırlandı."
    };

    private static final String[] PORTAL_YAPILACAK = {
            "Tasarım sistemi.",
            "Talep oluşturma akışı.",
            "Talep takibi.",
            "Navigasyon ve arama.",
            "İçerik yönetimi ve UAT hazırlığı.",
            "UAT ve içerik girişi."
    };

    private static final String[] AMBAR_YAPILAN = {
            "Kaynak sistem envanteri çıkarıldı.",
            "Bulut ortamı ve ağ yapılandırması kuruldu.",
            "Müşteri verisi taşındı, doğrulama yapıldı.",
            "Satış verisi için ETL akışları yazıldı.",
            "Raporlama katmanı yeni ambara bağlandı."
    };

    private static final String[] SAHA_YAPILAN = {
            "İş emri listesi ve detay ekranları.",
            "Fotoğraf ve imza toplama.",
            "Çevrimdışı veri saklama katmanı.",
            "Harita ve rota görünümü."
    };
}
