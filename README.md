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

## API Uç Noktaları

- `POST /api/admin/projects` — proje oluşturma, sorumlu PM ataması (ADMIN)
- `GET /api/projects` — PM kendi projelerini, CTO/Admin tüm projeleri görür
- `GET /api/projects/{id}` — proje detayı (PM yalnızca kendi projesi)
- `POST /api/projects/{id}/weekly-reports` — haftalık rapor oluşturma/güncelleme (PM; aynı proje+hafta güncelleme sayılır)
- `GET /api/projects/{id}/weekly-reports` — projenin geçmiş raporları
- `GET /api/dashboard/cto` — her projenin son durumu, `projectId` / `raporHaftasi` / `genelDurum` / `riskSeviyesi` ile filtrelenebilir (CTO)
- `POST /api/projects/{id}/tasks`, `PUT .../tasks/{taskId}`, `GET .../tasks` — iş kalemi yönetimi (PM)

Yetki kuralı: CTO yazma işlemi yapamaz, PM yalnızca sorumlusu olduğu projeye yazabilir.

## Frontend

Giriş sonrası kullanıcı rolüne göre otomatik yönlendirilir:

- `/login` — herkese açık giriş ekranı
- `/admin` — **Admin**: kullanıcı oluşturma (Admin/PM/CTO), proje oluşturma ve sorumlu PM ataması
- `/pm` — **PM**: kendine atanmış proje(ler) arasında seçim, haftalık rapor formu (aynı hafta ikinci girişte otomatik günceller), geçmiş rapor tablosu, iş kalemi ekleme ve durum güncelleme
- `/cto` — **CTO**: portföy özeti (yeşil/sarı/kırmızı proje sayaçları), proje/durum/risk filtreleri, renk kodlu proje kartları

Yetkisiz bir role ait sayfaya erişim denendiğinde ana sayfaya yönlendirilir; token geçersiz/süresi dolmuşsa otomatik olarak `/login`'e döner.

## Durum

MVP tamamlandı — backend, frontend, testler ve deployment doğrulandı. Canlı ortamda çalışıyor.

## Eksikler

- Otomatik (JUnit, Cypress/Playwright) testler henüz yok; manuel test akışı ve sonuçları `docs/test-senaryolari.md`'de
- Render'ın ücretsiz planı hareketsizlikte uykuya geçiyor, ücretsiz PostgreSQL 90 gün sonra siliniyor (bkz. `docs/deployment.md`)
