# Test Senaryoları ve Kanıtları

Backend API'sini PowerShell üzerinden manuel test ettim.
Aşağıda her senaryonun komutları, beklenen sonuç ve gerçek sonuç yer alıyor.

## Ön Koşul

Backend ayakta olmalı: `cd backend; mvn spring-boot:run`
Varsayılan adres: `http://localhost:8080`

---

## Senaryo 1 — İlk admin ile giriş

**Amaç:** Uygulama ilk ayağa kalktığında `.env`'deki admin otomatik oluşuyor mu, login çalışıyor mu.

```powershell
$body = @{ email = "admin@kolaysoft.com.tr"; sifre = "ChangeMe123!" } | ConvertTo-Json
$adminLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $body -ContentType "application/json"
$adminLogin
```

**Beklenen:** `token`, `userId`, `rol: "ADMIN"` alanlarını içeren bir JSON.

**Sonuç:** ✅ Başarılı. Login `200 OK` döndü, `token`/`userId`/`rol: ADMIN` doğru geldi.

Bu senaryoyu ilk kez çalıştırana kadar backend'i ayağa kaldırma sürecinde beş
farklı hatayla karşılaştım, hepsini çözdüm (detaylar aşağıdaki tabloda).

---

## Senaryo 2 — Admin, PM ve CTO kullanıcı oluşturur

```powershell
$headers = @{ Authorization = "Bearer $($adminLogin.token)" }

$pmBody = @{ adSoyad = "Ayşe PM"; email = "ayse.pm@kolaysoft.com.tr"; sifre = "Sifre123!"; rol = "PM" } | ConvertTo-Json
$pm = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Post -Body $pmBody -ContentType "application/json" -Headers $headers

$ctoBody = @{ adSoyad = "Can CTO"; email = "can.cto@kolaysoft.com.tr"; sifre = "Sifre123!"; rol = "CTO" } | ConvertTo-Json
$cto = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Post -Body $ctoBody -ContentType "application/json" -Headers $headers

$pm
$cto
```

**Beklenen:** İki kullanıcı da `201 Created` ile döner.

**Sonuç:** ✅ Başarılı. PM ve CTO kullanıcıları doğru rollerle oluşturuldu (`id 2` ve `id 3`).

**Negatif test (henüz koşulmadı):** Aynı email ile ikinci kez `POST /api/admin/users` çağrıldığında `409 Conflict` dönmesi bekleniyor.

---

## Senaryo 3 — Admin, PM'e proje atar

```powershell
$projectBody = @{ ad = "PatronMobil Demo"; musteri = "Math Cafe"; sorumluPmId = $pm.id; durum = "AKTIF" } | ConvertTo-Json
$project = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/projects" -Method Post -Body $projectBody -ContentType "application/json" -Headers $headers
$project
```

**Beklenen:** `201 Created`, `sorumluPmAdSoyad: "Ayşe PM"`.

**Sonuç:** ✅ Başarılı. Proje oluşturuldu, `sorumluPmAdSoyad: "Ayse PM"` doğru döndü.

**Negatif test (henüz koşulmadı):** `sorumluPmId` olarak CTO'nun id'si gönderildiğinde `400 Bad Request` dönmesi bekleniyor.

---

## Senaryo 4 — PM giriş yapar ve haftalık rapor girer

```powershell
$pmLoginBody = @{ email = "ayse.pm@kolaysoft.com.tr"; sifre = "Sifre123!" } | ConvertTo-Json
$pmLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $pmLoginBody -ContentType "application/json"
$pmHeaders = @{ Authorization = "Bearer $($pmLogin.token)" }

$reportBody = @{
    raporHaftasi = "2026-08-10"
    hedeflenenIlerleme = 40
    gerceklesenIlerleme = 35
    genelDurum = "SARI"
    riskSeviyesi = "ORTA"
    canliTask = "Giderler ekrani accordion UI"
    yapilanlar = "Dashboard grafikleri tamamlandi"
    yapilacaklar = "Cari hesap iskonto testleri"
    riskEngelNotu = "PDF export icin ek kutuphane arastirmasi gerekiyor"
    genelNot = "Genel olarak plan dahilinde"
} | ConvertTo-Json

$report = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/weekly-reports" -Method Post -Body $reportBody -ContentType "application/json" -Headers $pmHeaders
$report
```

**Beklenen:** `200 OK`, rapor bilgileri geri döner.

**Sonuç:** ✅ Başarılı. Rapor oluşturuldu, tüm alanlar doğru geldi.

**Upsert testi:** Aynı `raporHaftasi` (`2026-08-10`) için `gerceklesenIlerleme`yi `50`'ye değiştirip isteği tekrar gönderdim.

**Sonuç:** ✅ Başarılı. `id` değişmedi (`1` kaldı), `gerceklesenIlerleme` `50`'ye güncellendi, `GET /api/projects/{id}/weekly-reports` ile listelendiğinde toplam kayıt sayısı `1` çıktı (2 olmadı) — upsert mantığı doğrulandı.

**Negatif test:** `hedeflenenIlerleme` alanına `150` gönderdim.

**Sonuç:** ✅ Başarılı. `400 Bad Request` döndü.

---

## Senaryo 5 — Yetkilendirme sınırları

```powershell
$ctoLoginBody = @{ email = "can.cto@kolaysoft.com.tr"; sifre = "Sifre123!" } | ConvertTo-Json
$ctoLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $ctoLoginBody -ContentType "application/json"
$ctoHeaders = @{ Authorization = "Bearer $($ctoLogin.token)" }

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/weekly-reports" -Method Post -Body $reportBody -ContentType "application/json" -Headers $ctoHeaders
} catch {
    $_.Exception.Response.StatusCode
}
```

**Beklenen:** `403 Forbidden` (CTO yazma yapamamalı).

**Sonuç:** ✅ Başarılı. `403 Forbidden` döndü.

**Diğer negatif testler (henüz koşulmadı):**
- Token olmadan `GET /api/projects` çağrıldığında `401 Unauthorized` dönmesi bekleniyor
- Yanlış şifreyle login denendiğinde `401 Unauthorized` dönmesi bekleniyor
- Başka bir PM'in token'ıyla bu projeye yazma denendiğinde `403 Forbidden` dönmesi bekleniyor

---

## Senaryo 6 — CTO dashboard ve filtreleme

```powershell
$dashboard = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/cto" -Method Get -Headers $ctoHeaders
$dashboard

$filtered = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/cto?riskSeviyesi=DUSUK" -Method Get -Headers $ctoHeaders
$filtered

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/cto" -Method Get -Headers $pmHeaders
} catch {
    $_.Exception.Response.StatusCode
}
```

**Beklenen:** Tüm projelerin en son raporu listelenir; `riskSeviyesi` filtresi doğru eşleşir; PM bu endpoint'e erişemez.

**Sonuç:** ✅ Başarılı. Dashboard doğru veriyi döndürdü, `riskSeviyesi=DUSUK` filtresi tek kayıtla doğru eşleşti, PM'in dashboard'a erişim denemesi `403 Forbidden` ile engellendi.

---

## Senaryo 7 — İş kalemi (task) akışı

```powershell
$taskBody = @{ baslik = "PDF export arastirmasi"; durum = "DEVAM_EDIYOR"; sorumlu = "Ayşe PM" } | ConvertTo-Json
$task = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/tasks" -Method Post -Body $taskBody -ContentType "application/json" -Headers $pmHeaders
$task

$updateBody = @{ baslik = "PDF export arastirmasi"; durum = "TAMAMLANDI"; sorumlu = "Ayşe PM" } | ConvertTo-Json
$updatedTask = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/tasks/$($task.id)" -Method Put -Body $updateBody -ContentType "application/json" -Headers $pmHeaders
$updatedTask.durum
```

**Beklenen:** Task oluşturulur (`201`), sonra durumu `TAMAMLANDI` olarak güncellenir.

**Sonuç:** ✅ Başarılı. Task oluşturuldu, `PUT` ile durum `DEVAM_EDIYOR` → `TAMAMLANDI` olarak güncellendi.

---

## Test Sonuçları Kaydı

| Tarih | Senaryo | Sonuç | Not / Bulunan Hata |
|---|---|---|---|
| 2026-08-13 | Senaryo 1 — İlk admin ile giriş | ✅ Başarılı | Backend'i ilk kez ayağa kaldırma sürecinde şu hatalarla karşılaştım ve çözdüm: (1) Lombok, JDK 25 ile annotation processing yapamıyordu → Lombok 1.18.42'ye sabitledim. (2) Spring Security 7'de `DaoAuthenticationProvider`'ın no-arg constructor'ı kaldırılmış → constructor'a `UserDetailsService` verdim. (3) `OncePerRequestFilter`'daki `@NonNull` annotation'ları Spring Framework 7 ile çakışıyordu → kaldırdım. (4) `TaskItem` entity'sindeki `not` alanı PostgreSQL'in ayrılmış kelimesiyle çakışıp tablo oluşturmayı bozuyordu → sütun adını `notlar` yaptım. (5) PostgreSQL kurulumu Türkçe Windows locale hatası verdi → kurulumda `C` locale seçerek çözdüm. |
| 2026-08-13 | Senaryo 2 — Admin, PM ve CTO oluşturur | ✅ Başarılı | İki kullanıcı da `201 Created` ile oluştu, doğru rollerle döndü. |
| 2026-08-13 | Senaryo 3 — Admin, PM'e proje atar | ✅ Başarılı | Proje oluşturuldu, `sorumluPmAdSoyad` doğru döndü. |
| 2026-08-13 | Senaryo 4 — PM haftalık rapor girer + upsert | ✅ Başarılı | Upsert mantığı doğrulandı (aynı proje+hafta ikinci kez gönderildiğinde günceller, yeni kayıt açmaz). Negatif validasyon testi (`hedeflenenIlerleme=150`) `400` ile doğru sonuç verdi. |
| 2026-08-13 | Senaryo 5 — Yetkilendirme sınırları | ✅ Başarılı | CTO'nun rapor yazma girişimi `403 Forbidden` ile engellendi. |
| 2026-08-13 | Senaryo 6 — CTO dashboard + filtreleme | ✅ Başarılı | Dashboard doğru veriyi döndürdü, filtre doğru eşleşti, PM'in dashboard'a erişim denemesi `403 Forbidden` ile engellendi. |
| 2026-08-13 | Senaryo 7 — İş kalemi (task) akışı | ✅ Başarılı | Task oluşturuldu ve durumu başarıyla güncellendi. |

**Özet:** 7/7 senaryo başarılı. Backend MVP'nin çekirdek akışlarını (auth, rol bazlı yetkilendirme, proje/rapor/task CRUD, upsert, CTO dashboard filtreleme) uçtan uca doğruladım.

## Bilinen Riskler / Kalan Test Eksikleri

- Otomatik (JUnit) testler henüz yazmadım, yukarıdaki 7 senaryoyu manuel yürüttüm
- Eşzamanlı (concurrent) upsert senaryosunu (iki PM aynı anda aynı rapora yazarsa) henüz test etmedim
- Task listeleme ve proje listeleme (PM'in yalnızca kendi projelerini görmesi) için ayrı bir PM ile çapraz erişim denemesini henüz koşmadım
- Şifre doğrulama (min 6 karakter), yanlış email/şifre ile login (`401`), token olmadan istek (`401`) senaryolarını henüz koşmadım
