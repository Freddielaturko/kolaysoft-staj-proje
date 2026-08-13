# Test Senaryoları ve Kanıtları

Backend API'sinin manuel test akışı ve beklenen sonuçlar. Test yaptıkça
sonuçları en alttaki tabloya (tarih, sonuç, varsa hata + düzeltme sonrası
tekrar test) işliyorum.

Komutlar PowerShell (`Invoke-RestMethod`) içindir. Postman/Insomnia kullanmak
istersen aynı adımları GUI üzerinden birebir uygulayabilirsin.

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

**Beklenen:** `token`, `userId`, `rol: "ADMIN"` alanlarını içeren bir JSON döner. `$adminToken = $adminLogin.token` ile sakla.

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

**Beklenen:** İki kullanıcı da `201 Created` ile döner, `id` alanları not edilir (bir sonraki adımda `sorumluPmId` için gerekecek).

**Negatif test:** Aynı email ile tekrar `POST /api/admin/users` çağır → `409 Conflict`, "Bu email ile kayitli bir kullanici zaten var" beklenir.

---

## Senaryo 3 — Admin, PM'e proje atar

```powershell
$projectBody = @{ ad = "PatronMobil Demo"; musteri = "Math Cafe"; sorumluPmId = $pm.id; durum = "AKTIF" } | ConvertTo-Json
$project = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/projects" -Method Post -Body $projectBody -ContentType "application/json" -Headers $headers
$project
```

**Beklenen:** `201 Created`, `sorumluPmAdSoyad: "Ayşe PM"` dönmeli.

**Negatif test:** `sorumluPmId` olarak CTO'nun id'sini gönder → `400 Bad Request`, "Secilen kullanici PM rolunde degil" beklenir.

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

**Upsert testi:** Aynı `raporHaftasi` (`2026-08-10`) ile farklı bir `gerceklesenIlerleme` (örn. 50) göndererek isteği tekrar at. **Beklenen:** yeni bir kayıt oluşmaz, mevcut rapor güncellenir (`GET /api/projects/{id}/weekly-reports` ile listelendiğinde tek kayıt olmalı).

**Negatif test:** `hedeflenenIlerleme` alanını `150` gönder → `400 Bad Request`, "Hedeflenen ilerleme 0-100 arasinda olmalidir" beklenir.

---

## Senaryo 5 — Yetkilendirme sınırları

```powershell
# CTO, rapor girmeye calisirsa (yazma) 403 donmeli
$ctoLoginBody = @{ email = "can.cto@kolaysoft.com.tr"; sifre = "Sifre123!" } | ConvertTo-Json
$ctoLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $ctoLoginBody -ContentType "application/json"
$ctoHeaders = @{ Authorization = "Bearer $($ctoLogin.token)" }

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/weekly-reports" -Method Post -Body $reportBody -ContentType "application/json" -Headers $ctoHeaders
} catch {
    $_.Exception.Response.StatusCode
}
```

**Beklenen:** `403 Forbidden`.

**Diğer negatif testler:**
- Token olmadan `GET /api/projects` çağır → `401 Unauthorized`
- Yanlış şifreyle login → `401 Unauthorized`, "Email veya sifre hatali"
- Başka bir PM oluşturup onun token'ıyla bu projeye yazmayı dene → `403 Forbidden`, "Bu projeye yazma yetkiniz yok"

---

## Senaryo 6 — CTO dashboard ve filtreleme

```powershell
$dashboard = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/cto" -Method Get -Headers $ctoHeaders
$dashboard

# Filtreli sorgu
$filtered = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/cto?riskSeviyesi=ORTA" -Method Get -Headers $ctoHeaders
$filtered
```

**Beklenen:** Tüm projelerin en son raporu listelenir; `riskSeviyesi=ORTA` filtresiyle yalnızca o risk seviyesindeki raporlar döner.

**Negatif test:** Bu endpoint'i PM token'ıyla çağır → `403 Forbidden` beklenir (`@PreAuthorize("hasRole('CTO')")`).

---

## Senaryo 7 — İş kalemi (task) akışı

```powershell
$taskBody = @{ baslik = "PDF export arastirmasi"; durum = "DEVAM_EDIYOR"; sorumlu = "Ayşe PM" } | ConvertTo-Json
$task = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/tasks" -Method Post -Body $taskBody -ContentType "application/json" -Headers $pmHeaders
$task

# Durum guncelleme
$updateBody = @{ baslik = "PDF export arastirmasi"; durum = "TAMAMLANDI"; sorumlu = "Ayşe PM" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($project.id)/tasks/$($task.id)" -Method Put -Body $updateBody -ContentType "application/json" -Headers $pmHeaders
```

**Beklenen:** Task oluşturulur (`201`), sonra durumu `TAMAMLANDI` olarak güncellenir (`200`).

---

## Test Sonuçları Kaydı

Aşağıdaki tabloyu her test turunda güncelle (yönetmelik madde 1.1 "test kanıtı" gereği):

| Tarih | Senaryo | Sonuç | Not / Bulunan Hata | Düzeltme Sonrası Tekrar Test |
|---|---|---|---|---|
| 2026-08-13 | Senaryo 1 — İlk admin ile giriş | ✅ Başarılı | Backend'i ilk kez ayağa kaldırma sürecinde şu hatalarla karşılaşıldı ve düzeltildi: (1) Lombok, JDK 25 ile annotation processing yapamıyordu → Lombok 1.18.42'ye sabitlendi. (2) Spring Security 7'de `DaoAuthenticationProvider`'ın no-arg constructor'ı kaldırılmış → constructor'a `UserDetailsService` verildi. (3) `OncePerRequestFilter`'daki `@NonNull` annotation'ları Spring Framework 7 ile çakışıyordu → kaldırıldı. (4) `TaskItem` entity'sindeki `not` alanı PostgreSQL'in ayrılmış kelimesiyle çakışıp tablo oluşturmayı bozuyordu → sütun adı `notlar` olarak değiştirildi. (5) PostgreSQL kurulumu Türkçe Windows locale hatası verdi → kurulumda `C` locale seçilerek çözüldü. | Login `200 OK` döndü, `token`/`userId`/`rol: ADMIN` doğru geldi. Tekrar teste gerek kalmadı, ilk denemede başarılı. |
| _(doldurulacak)_ | | | | |

## Bilinen Riskler / Kalan Test Eksikleri

- Otomatik (JUnit) testler henüz yazılmadı, yukarıdaki akış manuel yürütülüyor
- Eşzamanlı (concurrent) upsert senaryosu (iki PM aynı anda aynı rapora yazarsa) test edilmedi
