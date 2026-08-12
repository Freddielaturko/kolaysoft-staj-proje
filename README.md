# Haftalık Proje Durum Raporlama ve CTO Takip Sistemi

Kolaysoft staj final projem. Kolaysoft'ta proje durumları şu anda manuel olarak
PowerPoint üzerinden raporlanıyor; bu proje o süreci dijitalleştiriyor. Proje
yöneticileri haftalık ilerlemelerini sisteme giriyor, CTO tüm projeleri tek
ekrandan filtreleyerek takip edebiliyor.

- Analiz dokümanı: [`docs/analiz-dokumani.md`](docs/analiz-dokumani.md)
- Test senaryoları: [`docs/test-senaryolari.md`](docs/test-senaryolari.md)

## Teknoloji Yığını

- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Frontend:** React (Vite), React Router, Axios

## Proje Yapısı

```
backend/    Spring Boot API
frontend/   React SPA
docs/       Analiz dokümanı, test senaryoları, teknik karar notları
```

## Kurulum ve Çalıştırma

### Ön Koşullar
- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+ (yerel veya Docker)

### Backend

```bash
cd backend
cp .env.example .env   # değerleri kendi ortamına göre düzenle
# PostgreSQL'de projedurum adında bir veritabanı oluştur
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

## Durum

Geliştirme aşamasında — MVP (1-20. gün planı) üzerinde çalışıyorum.

## Bilinen Eksikler

- Frontend sayfaları (login, dashboard'lar, formlar) henüz yok
- Otomatik testler henüz yok, manuel test akışı `docs/test-senaryolari.md`'de
- CTO dashboard'a ek metrikler (görev sayacı vb.) eklenebilir
