package com.kolaysoft.projedurum.config;

import com.kolaysoft.projedurum.entity.Project;
import com.kolaysoft.projedurum.entity.TaskItem;
import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.WeeklyReport;
import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import com.kolaysoft.projedurum.entity.enums.RiskLevel;
import com.kolaysoft.projedurum.entity.enums.Role;
import com.kolaysoft.projedurum.entity.enums.TaskStatus;
import com.kolaysoft.projedurum.repository.ProjectRepository;
import com.kolaysoft.projedurum.repository.TaskItemRepository;
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
import java.util.List;

import static com.kolaysoft.projedurum.entity.enums.GenelDurum.KIRMIZI;
import static com.kolaysoft.projedurum.entity.enums.GenelDurum.SARI;
import static com.kolaysoft.projedurum.entity.enums.GenelDurum.YESIL;
import static com.kolaysoft.projedurum.entity.enums.RiskLevel.DUSUK;
import static com.kolaysoft.projedurum.entity.enums.RiskLevel.ORTA;
import static com.kolaysoft.projedurum.entity.enums.RiskLevel.YUKSEK;
import static com.kolaysoft.projedurum.entity.enums.TaskStatus.BEKLEMEDE;
import static com.kolaysoft.projedurum.entity.enums.TaskStatus.DEVAM_EDIYOR;
import static com.kolaysoft.projedurum.entity.enums.TaskStatus.TAMAMLANDI;

// APP_DEMO_ENABLED=true iken ve hic proje yokken calisir. Canli demoda bos bir ekran yerine
// bir yazilim sirketinin gercekci bir portfoyu gorunsun diye ekledim: ilerleme haftadan haftaya
// duzensiz, bazi haftalar yerinde sayiyor, raporlarda teknik detay var. Tarihler bugune gore
// hesaplandigi icin demo ne zaman acilirsa acilsin gecikmeler ve teslim uyarilari guncel kalir.
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository reportRepository;
    private final TaskItemRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectInsightService insightService;

    @Value("${app.demo.enabled:false}")
    private boolean enabled;

    @Value("${app.demo.password:demo123}")
    private String password;

    private LocalDate today;
    private LocalDate week;

    // Bir haftalik rapor satiri; "ago" kac hafta once girildigini gosteriyor (0 = bu hafta)
    private record W(int ago, int hedef, int gercek, GenelDurum durum, RiskLevel risk,
                     String yapilan, String yapilacak, String riskNotu,
                     String butce, String ekip, String canliTask, int revizyon) {
    }

    // Bir is kalemi; tarihler bugune gore gun farki
    private record T(String baslik, String aciklama, String sorumlu, TaskStatus durum,
                     int planlananGun, Integer tamamlananGun) {
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || projectRepository.count() > 0) {
            return;
        }

        today = insightService.today();
        week = insightService.currentWeek();

        user("admin@kolaysoft.demo", "Sistem Admin", Role.ADMIN);
        user("cto@kolaysoft.demo", "Salih Mert Anarol", Role.CTO);
        User zeynep = user("pm@kolaysoft.demo", "Zeynep Demir", Role.PM);
        User mert = user("pm2@kolaysoft.demo", "Mert Aydın", Role.PM);
        User ece = user("lead@kolaysoft.demo", "Ece Özkan", Role.LEAD);
        User canan = user("lead2@kolaysoft.demo", "Canan Kaya", Role.LEAD);

        // Kritik: hedefin gerisinde kaldi, GIB test ortaminda cozulemeyen hatalar var
        Project sema = project("KLF-318", "e-Fatura Şema Uyum Güncellemesi",
                "GİB'in yayımladığı yeni UBL-TR şema ve kontrol setine fatura oluşturma, doğrulama ve gönderim servislerinin uyarlanması.",
                "Kolaysoft", "e-Dönüşüm Ürünleri", 385_000L, 19, zeynep, ece, ProjectStatus.AKTIF);
        reports(sema, ece, List.of(
                new W(7, 8, 7, YESIL, DUSUK, "Yeni şema dokümanı incelendi, etkilenen 14 alan ve 6 kontrol kuralı listelendi.", "XSD dosyalarının projeye alınması ve doğrulama katmanının güncellenmesi.", null, "Plan dahilinde", "3 geliştirici, 1 test", "KLF-318-04 Şema analizi", 1),
                new W(6, 16, 15, YESIL, DUSUK, "XSD ve şematron dosyaları entegre edildi, doğrulama servisi yeni kuralları okuyor.", "Fatura oluşturma ekranında yeni zorunlu alanlar.", null, "Plan dahilinde", "3 geliştirici, 1 test", "KLF-318-09 Doğrulama servisi", 1),
                new W(5, 25, 22, YESIL, ORTA, "Fatura oluşturma ekranına 4 yeni zorunlu alan eklendi, mevcut taslaklar için varsayılan değerler tanımlandı.", "Gönderim servisinin GİB test ortamına bağlanması.", "Eski şemayla kaydedilmiş taslak faturaların dönüşümü ek efor gerektiriyor", "Plan dahilinde", "3 geliştirici, 1 test", "KLF-318-12 Zorunlu alanlar", 1),
                new W(4, 34, 28, SARI, ORTA, "Gönderim servisi GİB test ortamına bağlandı, ilk 200 test faturası gönderildi.", "Test ortamından dönen hata kodlarının çözümü.", "GİB test ortamı hafta boyunca iki kez erişilemez oldu\nTaslak dönüşüm betiği beklenenden yavaş", "Plan dahilinde", "3 geliştirici, 1 test", "KLF-318-15 GİB test entegrasyonu", 2),
                new W(3, 43, 33, SARI, ORTA, "Test ortamından dönen 11 hata kodunun 7'si çözüldü.", "Kalan hata kodları ve dönüşüm betiğinin optimizasyonu.", "4 hata kodu için GİB'den yazılı açıklama bekleniyor", "%5 sapma riski", "3 geliştirici, 1 test", "KLF-318-15 GİB test entegrasyonu", 1),
                new W(2, 52, 36, KIRMIZI, YUKSEK, "Dönüşüm betiği paralel çalışacak şekilde yeniden yazıldı, kalan hata kodlarından 1'i çözüldü.", "Performans testi ve GİB yanıtına göre kalan hatalar.", "GİB test ortamında 3 hata kodu çözülemedi\nDönüşüm betiği saatte 12 bin fatura işliyor, hedef 40 bin", "%9 sapma (ek test eforu)", "Bir geliştirici başka projeye destek verdi", "KLF-318-18 Dönüşüm performansı", 2),
                new W(1, 60, 40, KIRMIZI, YUKSEK, "Performans testi yapıldı, veritabanı tarafında toplu yazma ile hız saatte 21 bine çıktı.", "Dönüşümün gece penceresine sığması için iş bölme ve kalan hata kodları.", "GİB test ortamında 3 hata kodu çözülemedi\nDönüşüm betiği hâlâ hedefin yarısı hızında", "%9 sapma (ek test eforu)", "Kapasite kritik", "KLF-318-18 Dönüşüm performansı", 1),
                new W(0, 68, 44, KIRMIZI, YUKSEK, "GİB'den gelen açıklamayla 2 hata kodu kapatıldı. Dönüşüm 4 paralel işe bölündü.", "Son hata kodu, uçtan uca regresyon testi ve canlı geçiş planı.", "Teslim tarihine kadar regresyon testine 3 gün kalıyor\nSon hata kodu için GİB yanıtı bekleniyor", "%11 sapma", "Kapasite kritik", "KLF-318-21 Regresyon testi", 1)
        ));
        tasks(sema, List.of(
                new T("Yeni XSD ve şematron dosyalarının entegrasyonu", null, "Burak Şahin", TAMAMLANDI, -40, -38),
                new T("Fatura ekranına zorunlu alanların eklenmesi", "Taslak faturalar için varsayılan değer ataması dahil", "Elif Arslan", TAMAMLANDI, -30, -29),
                new T("GİB test ortamı hata kodlarının çözümü", "11 hata kodundan 10'u kapandı", "Burak Şahin", DEVAM_EDIYOR, -7, null),
                new T("Taslak fatura dönüşüm betiği performansı", "Hedef saatte 40 bin fatura", "Onur Çelik", DEVAM_EDIYOR, -3, null),
                new T("Uçtan uca regresyon testi", null, "Selin Koç", BEKLEMEDE, 12, null),
                new T("Canlı geçiş ve geri dönüş planı", null, "Ece Özkan", BEKLEMEDE, 17, null)
        ));

        // Dikkat: musteri tarafindaki gecikme yuzunden sari
        Project logo = project("KLF-305", "Logo Tiger e-İrsaliye Entegrasyonu",
                "Logo Tiger kullanan kurumsal müşteriler için irsaliyelerin ERP'den otomatik alınıp e-İrsaliye olarak gönderilmesi.",
                "Akdeniz Gıda San. A.Ş.", "Entegrasyon", 210_000L, 33, zeynep, canan, ProjectStatus.AKTIF);
        reports(logo, canan, List.of(
                new W(5, 12, 12, YESIL, DUSUK, "Müşteriyle analiz toplantısı yapıldı, 3 depo ve 2 irsaliye tipi kapsama alındı.", "Logo veritabanı görünümlerinin çıkarılması.", null, "Plan dahilinde", "2 geliştirici", "KLF-305-02 Analiz", 1),
                new W(4, 24, 25, YESIL, DUSUK, "Logo tarafındaki irsaliye görünümleri hazırlandı, okuma servisi yazıldı.", "Eşleştirme kuralları ve e-İrsaliye UBL üretimi.", null, "Plan dahilinde", "2 geliştirici", "KLF-305-05 Okuma servisi", 1),
                new W(3, 36, 38, YESIL, DUSUK, "Birim, depo ve cari eşleştirme kuralları tamamlandı. UBL üretimi test verisiyle çalışıyor.", "Müşterinin test ortamında uçtan uca deneme.", null, "Plan dahilinde", "2 geliştirici", "KLF-305-07 UBL üretimi", 1),
                new W(2, 48, 44, SARI, ORTA, "Hata durumları için yeniden deneme kuyruğu eklendi.", "Müşteri test ortamında kurulum.", "Müşterinin test sunucusu hazır değil, kurulum 1 hafta kaydı", "Plan dahilinde", "2 geliştirici", "KLF-305-09 Yeniden deneme kuyruğu", 1),
                new W(1, 58, 49, SARI, ORTA, "Kurulum paketi ve kurulum dokümanı hazırlandı.", "Test sunucusu açılınca kurulum ve kabul testi.", "Müşterinin test sunucusu hâlâ hazır değil", "Plan dahilinde", "1 geliştirici başka projede", "KLF-305-10 Kurulum paketi", 1),
                new W(0, 66, 55, SARI, ORTA, "Müşteri test sunucusunu açtı, kurulum yapıldı, ilk 40 irsaliye başarıyla gönderildi.", "Kabul testi ve müşteri ekibinin eğitimi.", "Kabul testi için müşteri tarafından yalnızca 2 gün ayrılabildi", "Plan dahilinde", "2 geliştirici", "KLF-305-11 Kabul testi", 1)
        ));
        tasks(logo, List.of(
                new T("Logo irsaliye görünümlerinin hazırlanması", null, "Deniz Yıldız", TAMAMLANDI, -28, -27),
                new T("Depo, birim ve cari eşleştirme kuralları", null, "Deniz Yıldız", TAMAMLANDI, -21, -20),
                new T("Hatalı gönderimler için yeniden deneme kuyruğu", null, "Emre Kurt", TAMAMLANDI, -14, -12),
                new T("Müşteri test ortamında kabul testi", "İlk 40 irsaliye başarılı", "Canan Kaya", DEVAM_EDIYOR, 6, null),
                new T("Müşteri ekibine kullanıcı eğitimi", null, "Zeynep Demir", BEKLEMEDE, 14, null)
        ));

        // Normal: plana uygun ilerleyen, raporlari duzenli proje
        Project arsiv = project("KLF-297", "e-Arşiv Toplu Gönderim Servisi",
                "Yüksek hacimli müşteriler için e-Arşiv faturalarının kuyruk üzerinden toplu oluşturulup raporlanması.",
                "Kolaysoft", "e-Dönüşüm Ürünleri", 265_000L, 54, mert, canan, ProjectStatus.AKTIF);
        reports(arsiv, canan, List.of(
                new W(9, 6, 6, YESIL, DUSUK, "Mimari doküman hazırlandı, kuyruk için RabbitMQ seçildi.", "Kuyruk altyapısının kurulması.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-01 Mimari", 1),
                new W(8, 13, 12, YESIL, DUSUK, "Kuyruk altyapısı ve tüketici servis iskeleti kuruldu.", "Fatura oluşturma işinin kuyruğa taşınması.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-03 Kuyruk altyapısı", 1),
                new W(7, 20, 19, YESIL, DUSUK, "Fatura oluşturma işi kuyruğa taşındı, tekrar eden mesajlar için idempotency anahtarı eklendi.", "PDF üretiminin ayrı işe ayrılması.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-05 Kuyruk tüketicisi", 1),
                new W(6, 27, 27, YESIL, DUSUK, "PDF üretimi ayrı işe ayrıldı, şablon motoru önbelleğe alındı.", "Yük testi.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-07 PDF işi", 1),
                new W(5, 34, 31, YESIL, ORTA, "İlk yük testi yapıldı: dakikada 3 bin fatura.", "Darboğaz analizi.", "PDF üretimi hedefin altında kaldı", "Plan dahilinde", "3 geliştirici", "KLF-297-08 Yük testi", 1),
                new W(4, 41, 39, YESIL, DUSUK, "PDF işi yatayda ölçeklendi, dakikada 7 bin faturaya çıkıldı.", "Gönderim raporu ekranı.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-08 Yük testi", 2),
                new W(3, 48, 47, YESIL, DUSUK, "Gönderim raporu ekranı ve CSV dışa aktarma tamamlandı.", "Hata durumlarında müşteriye e-posta bildirimi.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-10 Rapor ekranı", 1),
                new W(2, 55, 54, YESIL, DUSUK, "Bildirim servisi bağlandı, hatalı faturalar ayrı listede toplanıyor.", "Pilot müşteri seçimi ve izleme panoları.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-11 Bildirimler", 1),
                new W(1, 62, 61, YESIL, DUSUK, "Grafana panoları hazırlandı, iki pilot müşteri belirlendi.", "Pilot müşterilerde kontrollü açılış.", null, "Plan dahilinde", "3 geliştirici", "KLF-297-13 İzleme", 1)
        ));
        tasks(arsiv, List.of(
                new T("Kuyruk altyapısı ve tüketici servis", null, "Emre Kurt", TAMAMLANDI, -55, -54),
                new T("PDF üretiminin ayrı işe taşınması", null, "Selin Koç", TAMAMLANDI, -42, -41),
                new T("Yük testi ve darboğaz giderme", "Dakikada 7 bin fatura", "Emre Kurt", TAMAMLANDI, -28, -26),
                new T("Gönderim raporu ekranı", null, "Elif Arslan", TAMAMLANDI, -21, -20),
                new T("Pilot müşterilerde kontrollü açılış", null, "Canan Kaya", DEVAM_EDIYOR, 9, null),
                new T("Tüm müşterilere açılış", null, "Mert Aydın", BEKLEMEDE, 45, null)
        ));

        // Dikkat: raporlar yesil ama teslime az kaldi ve ilerleme %80'in altinda
        Project defter = project("KLF-309", "e-Defter Berat Yükleme Otomasyonu",
                "Mali müşavir kullanıcıları için e-Defter beratlarının imzalanıp GİB'e toplu yüklenmesinin otomatikleştirilmesi.",
                "Kolaysoft", "e-Dönüşüm Ürünleri", 175_000L, 9, mert, ece, ProjectStatus.AKTIF);
        reports(defter, ece, List.of(
                new W(5, 15, 14, YESIL, DUSUK, "Mali müşavirlerle görüşüldü, toplu yükleme akışı çıkarıldı.", "Mali mühür ile imzalama servisi.", null, "Plan dahilinde", "2 geliştirici", "KLF-309-02 Analiz", 1),
                new W(4, 30, 27, YESIL, DUSUK, "İmzalama servisi mali mühür ile çalışıyor.", "Berat yükleme kuyruğu.", null, "Plan dahilinde", "2 geliştirici", "KLF-309-04 İmzalama", 1),
                new W(3, 45, 41, YESIL, ORTA, "Yükleme kuyruğu yazıldı, GİB yanıtları kaydediliyor.", "Mükellef bazlı durum ekranı.", "Ay sonu yoğunluğunda GİB yanıt süreleri uzuyor", "Plan dahilinde", "2 geliştirici", "KLF-309-06 Yükleme kuyruğu", 1),
                new W(2, 58, 55, YESIL, ORTA, "Durum ekranı tamamlandı, başarısız yüklemeler tekrar denenebiliyor.", "Pilot mali müşavir ofisinde deneme.", null, "Plan dahilinde", "2 geliştirici", "KLF-309-07 Durum ekranı", 1),
                new W(1, 70, 66, YESIL, ORTA, "Pilot ofiste 38 mükellefin beratı yüklendi.", "Pilot geri bildirimleri ve toplu seçim iyileştirmesi.", null, "Plan dahilinde", "2 geliştirici", "KLF-309-08 Pilot", 1),
                new W(0, 80, 74, YESIL, ORTA, "Geri bildirimle toplu seçim ve filtreleme eklendi.", "Kullanım kılavuzu ve yayına alma.", "Ay sonu öncesi yayına alınması gerekiyor", "Plan dahilinde", "2 geliştirici", "KLF-309-09 İyileştirmeler", 1)
        ));
        tasks(defter, List.of(
                new T("Mali mühür ile imzalama servisi", null, "Onur Çelik", TAMAMLANDI, -26, -25),
                new T("Berat yükleme kuyruğu", null, "Onur Çelik", TAMAMLANDI, -19, -18),
                new T("Mükellef bazlı durum ekranı", null, "Elif Arslan", TAMAMLANDI, -12, -11),
                new T("Toplu seçim ve filtreleme", "Pilot ofis geri bildirimi", "Elif Arslan", DEVAM_EDIYOR, 2, null),
                new T("Kullanım kılavuzu", null, "Ece Özkan", BEKLEMEDE, 6, null)
        ));

        // Dikkat: son raporu 3 hafta once girilmis, rapor gecikmesi uyarisi uretir
        Project kvkk = project("KLF-301", "KVKK Erişim Kayıtları ve Denetim Modülü",
                "Kişisel veriye erişen tüm işlemlerin kayıt altına alınması ve denetçiler için raporlanması.",
                "Kolaysoft", "Bilgi Güvenliği", 150_000L, 47, zeynep, canan, ProjectStatus.AKTIF);
        reports(kvkk, zeynep, List.of(
                new W(6, 10, 10, YESIL, DUSUK, "Kişisel veri içeren tablolar envanteri çıkarıldı.", "Erişim kaydı tasarımı.", null, "Plan dahilinde", "1 geliştirici", "KLF-301-01 Envanter", 1),
                new W(5, 20, 18, YESIL, DUSUK, "Erişim kayıtları için ayrı şema ve saklama süresi kuralları tanımlandı.", "Servislere kayıt ara katmanının eklenmesi.", null, "Plan dahilinde", "1 geliştirici", "KLF-301-03 Kayıt şeması", 1),
                new W(3, 35, 29, SARI, ORTA, "Kayıt ara katmanı müşteri ve fatura servislerine eklendi.", "Kalan servisler ve denetçi rapor ekranı.", "Tek geliştirici e-Fatura şema projesine destek veriyor", "Plan dahilinde", "Kapasite sınırda", "KLF-301-05 Ara katman", 1)
        ));
        tasks(kvkk, List.of(
                new T("Kişisel veri envanteri", null, "Deniz Yıldız", TAMAMLANDI, -42, -41),
                new T("Erişim kaydı ara katmanı", "Müşteri ve fatura servisleri tamamlandı", "Deniz Yıldız", DEVAM_EDIYOR, -5, null),
                new T("Denetçi rapor ekranı", null, "Deniz Yıldız", BEKLEMEDE, 20, null)
        ));

        // Normal: yeni baslamis proje
        Project portal = project("KLF-321", "Müşteri Portalı Yenileme",
                "Müşteri portalının React ile yeniden yazılması; fatura arama, toplu indirme ve kullanıcı yönetimi.",
                "Kolaysoft", "Ürün & Tasarım", 330_000L, 82, mert, ece, ProjectStatus.AKTIF);
        reports(portal, mert, List.of(
                new W(3, 5, 5, YESIL, DUSUK, "Mevcut portal kullanım verileri incelendi, en sık kullanılan 6 akış belirlendi.", "Tasarım sistemi ve ilk ekran taslakları.", null, "Plan dahilinde", "2 geliştirici, 1 tasarımcı", "KLF-321-01 Keşif", 1),
                new W(2, 11, 10, YESIL, DUSUK, "Tasarım sistemi ve fatura arama ekranı taslağı onaylandı.", "Proje iskeleti ve kimlik doğrulama.", null, "Plan dahilinde", "2 geliştirici, 1 tasarımcı", "KLF-321-03 Tasarım", 1),
                new W(1, 17, 16, YESIL, DUSUK, "React iskeleti kuruldu, mevcut kimlik doğrulama servisine bağlandı.", "Fatura arama ekranı.", null, "Plan dahilinde", "2 geliştirici, 1 tasarımcı", "KLF-321-05 İskelet", 1)
        ));
        tasks(portal, List.of(
                new T("Tasarım sistemi", null, "Selin Koç", TAMAMLANDI, -12, -11),
                new T("Fatura arama ve filtreleme ekranı", null, "Burak Şahin", DEVAM_EDIYOR, 10, null),
                new T("Toplu PDF/XML indirme", null, "Burak Şahin", BEKLEMEDE, 30, null),
                new T("Kullanıcı ve yetki yönetimi", null, "Elif Arslan", BEKLEMEDE, 50, null)
        ));

        // Tamamlanan proje
        Project mobil = project("KLF-288", "Mobil e-İrsaliye Uygulaması",
                "Saha sevkiyat ekiplerinin irsaliyeyi telefondan düzenleyip imzalatabildiği Android ve iOS uygulaması.",
                "Kolaysoft", "Mobil", 290_000L, -9, zeynep, ece, ProjectStatus.TAMAMLANDI);
        reports(mobil, ece, List.of(
                new W(3, 88, 86, YESIL, DUSUK, "Mağaza incelemesi için sürüm gönderildi.", "Mağaza onayı ve pilot kullanıcılar.", null, "Plan dahilinde", "2 geliştirici, 1 test", "KLF-288-30 Yayın", 1),
                new W(2, 95, 94, YESIL, DUSUK, "İki mağazada da onay alındı, pilot ekip kullanmaya başladı.", "Pilot hataları ve genel duyuru.", null, "Plan dahilinde", "2 geliştirici, 1 test", "KLF-288-31 Pilot", 1),
                new W(1, 100, 100, YESIL, DUSUK, "Pilot hataları giderildi, tüm müşterilere duyuruldu.", "Proje kapanışı.", null, "%3 bütçe altında", "Ekip yeni projelere geçti", null, 1)
        ));

        // Arsivlenmis eski proje
        Project rest = project("KLF-274", "SOAP Servislerinin REST'e Taşınması",
                "Entegratör API'sindeki eski SOAP servislerinin REST karşılıklarının yazılması ve müşterilerin geçişi.",
                "Kolaysoft", "Platform", 240_000L, -128, mert, canan, ProjectStatus.ARSIV);
        reports(rest, canan, List.of(
                new W(19, 100, 100, YESIL, DUSUK, "Son müşteri de REST API'ye geçti, SOAP uçları kapatıldı.", "Arşiv.", null, "Plan dahilinde", "Ekip yeni projelere geçti", null, 1)
        ));

        log.info("Demo verisi yuklendi: {} kullanici, {} proje, {} rapor, {} is kalemi",
                userRepository.count(), projectRepository.count(), reportRepository.count(), taskRepository.count());
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

    private Project project(String kod, String ad, String aciklama, String musteri, String departman,
                            Long butce, int teslimGun, User pm, User lead, ProjectStatus durum) {
        Project p = new Project();
        p.setKod(kod);
        p.setAd(ad);
        p.setAciklama(aciklama);
        p.setMusteri(musteri);
        p.setDepartman(departman);
        p.setButce(butce);
        p.setTeslimTarihi(today.plusDays(teslimGun));
        p.setSorumluPm(pm);
        p.setEkipLideri(lead);
        p.setDurum(durum);
        return projectRepository.save(p);
    }

    private void reports(Project p, User author, List<W> weeks) {
        for (W w : weeks) {
            WeeklyReport r = new WeeklyReport();
            r.setProject(p);
            r.setOlusturanUser(author);
            r.setRaporHaftasi(week.minusWeeks(w.ago()));
            r.setHedeflenenIlerleme(w.hedef());
            r.setGerceklesenIlerleme(w.gercek());
            r.setGenelDurum(w.durum());
            r.setRiskSeviyesi(w.risk());
            r.setYapilanlar(w.yapilan());
            r.setYapilacaklar(w.yapilacak());
            r.setRiskEngelNotu(w.riskNotu());
            r.setButceDurumu(w.butce());
            r.setEkipDurumu(w.ekip());
            r.setCanliTask(w.canliTask());
            r.setRevizyon(w.revizyon());
            reportRepository.save(r);
        }
    }

    private void tasks(Project p, List<T> items) {
        for (T t : items) {
            TaskItem task = new TaskItem();
            task.setProject(p);
            task.setBaslik(t.baslik());
            task.setAciklama(t.aciklama());
            task.setSorumlu(t.sorumlu());
            task.setDurum(t.durum());
            task.setPlanlananTarih(today.plusDays(t.planlananGun()));
            task.setTamamlananTarih(t.tamamlananGun() == null ? null : today.plusDays(t.tamamlananGun()));
            taskRepository.save(task);
        }
    }
}
