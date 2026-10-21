# Haftalık Proje Durum Raporlama ve CTO Takip Sistemi

Kolaysoft staj final projem. Kolaysoft'ta proje durumları şu anda manuel olarak
PowerPoint üzerinden raporlanıyor; bu proje o süreci dijitalleştiriyor. Proje
yöneticileri haftalık ilerlemelerini sisteme giriyor, CTO tüm projeleri tek
ekrandan filtreleyerek takip edebiliyor.

- Analiz dokümanı: [`docs/analiz-dokumani.md`](docs/analiz-dokumani.md)
- Test senaryoları: [`docs/test-senaryolari.md`](docs/test-senaryolari.md)
- Deployment: [`docs/deployment.md`](docs/deployment.md)

## Canlı Adresler

- **Uygulama:** https://kolaysoft-staj-proje.vercel.app
- **Backend API:** https://kolaysoft-staj-proje.onrender.com/api

## Teknoloji Yığını

- **Backend:** Java 25, Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Frontend:** React (Vite), React Router, Axios

## Proje Yapısı

```
backend/    Spring Boot API
frontend/   React SPA
docs/       Analiz dokümanı, test senaryoları, teknik karar notları
```

## Kurulum ve Çalıştırma

### Ön Koşullar
- Java 25+ (LTS)
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+ (yerel veya Docker)

### Backend

```bash
cd backend
cp .env.example .env 
mvn spring-boot:run
```

API `http://localhost:8080` üzerinde ayağa kalkar.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Uygulama `http://localhost:5173` üzerinden erişilebilir.

## Kimlik Doğrulama

Herkese açık kayıt yok; kullanıcıları yalnızca Admin oluşturur.

- İlk admin: uygulama ilk kez ayağa kalktığında `users` tablosu boşsa,
  `.env`'deki `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` ile otomatik oluşturulur.
  İlk girişten sonra bu şifre değiştirilmeli.
- `POST /api/auth/login` — email + şifre ile giriş, JWT döner.
- `POST /api/admin/users` — yeni kullanıcı oluşturma (ADMIN).
- `GET /api/admin/users` — kullanıcı listesi (ADMIN).
- Diğer tüm `/api/**` endpoint'leri geçerli bir JWT gerektirir.

## Roller

- **Admin:** kullanıcı ve proje oluşturur, projeye PM ve ekip lideri atar, tüm projeleri görür
- **CTO:** tüm projeleri, raporları ve uyarıları görür; yazma işlemi yapamaz
- **Proje Yöneticisi (PM):** sorumlusu olduğu projeleri görür, haftalık rapor ve iş kalemi girer
- **Ekip Lideri:** lideri olduğu projeleri görür, haftalık rapor ve iş kalemi girer

## API Uç Noktaları

- `POST /api/admin/projects`, `PUT /api/admin/projects/{id}` — proje oluşturma ve güncelleme (ADMIN)
- `GET /api/projects` — rolün erişebildiği projeler; sağlık durumu, ilerleme, riskler ve rapor gecikmesi son rapordan hesaplanmış olarak döner
- `GET /api/projects/{id}` — proje detayı
- `POST /api/projects/{id}/weekly-reports` — haftalık rapor oluşturma/güncelleme (PM, Ekip Lideri). Aynı proje ve hafta için ikinci kayıt mevcut raporu günceller ve revizyon numarasını artırır
- `GET /api/projects/{id}/weekly-reports` — projenin geçmiş raporları
- `GET /api/reports` — rolün erişebildiği tüm projelerin raporları
- `GET /api/dashboard/summary` — KPI'lar ve son 12 haftanın portföy ilerleme trendi
- `GET /api/alerts` — kırmızı/sarı raporlardan, yaklaşan veya geçen teslim tarihlerinden ve eksik raporlardan üretilen uyarılar
- `GET /api/dashboard/cto` — her projenin son raporu, filtrelenebilir (CTO)
- `POST /api/projects/{id}/tasks`, `PUT .../tasks/{taskId}`, `GET .../tasks` — iş kalemi yönetimi

Projenin sağlık durumunu ayrı bir kolonda tutmuyorum. Son haftalık raporun genel durumu, teslim tarihi ve rapor düzeni üzerinden her istekte hesaplanıyor. Böylece rapor girildiği anda dashboard da güncelleniyor:

- **Kritik:** son rapor kırmızı ya da teslim tarihi geçmiş
- **Dikkat:** son rapor sarı, geçen haftanın raporu eksik ya da teslime 14 günden az kalmışken ilerleme %80'in altında
- **Arşiv:** arşivlenmiş veya iptal edilmiş proje

## Frontend

- `/login` — giriş ekranı. `VITE_DEMO_MODE=true` iken demo rol butonları görünür
- `/` — dashboard: KPI kartları, uyarı merkezi, haftalık ilerleme grafiği, proje tablosu
- `/projeler` — filtrelenebilir proje listesi, Admin için proje ekleme
- `/projeler/:id` — proje detayı: ilerleme, teslim ve bütçe özeti, haftalık rapor zaman çizelgesi, riskler, iş kalemleri
- `/raporlar` — tüm haftalık raporlar, rapor girişi
- `/kullanicilar` — kullanıcı yönetimi (Admin)

Token geçersiz veya süresi dolmuşsa otomatik olarak `/login`'e dönülür.

## Demo Verisi

`APP_DEMO_ENABLED=true` ile başlatıldığında, veritabanında hiç proje yoksa örnek kullanıcılar, altı proje ve geçmiş haftalık raporlar yüklenir. Tarihler uygulamanın başladığı güne göre hesaplandığı için demo ne zaman açılırsa açılsın gecikmeler ve teslim uyarıları gerçekçi görünür. Demo hesapları `cto@`, `pm@`, `pm2@`, `lead@`, `lead2@` ve `admin@kolaysoft.demo`, şifreleri `APP_DEMO_PASSWORD` (varsayılan `demo123`).

## Eksikler

- Otomatik (JUnit, Cypress/Playwright) testler henüz yok; manuel test akışı ve sonuçları `docs/test-senaryolari.md`'de
- Render'ın ücretsiz planı hareketsizlikte uykuya geçiyor (bkz. `docs/deployment.md`)
