# Haftalık Proje Durum Raporlama ve CTO Takip Sistemi

Kolaysoft staj final projesi. Kolaysoft'taki manuel PowerPoint tabanlı haftalık
proje raporlama sürecini dijitalleştiren full-stack sistem.

Detaylı analiz için: [`docs/analiz-dokumani.md`](docs/analiz-dokumani.md)

## Teknoloji Yığını

- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Frontend:** React (Vite), React Router, Axios

## Proje Yapısı

```
backend/    Spring Boot API
frontend/   React SPA
docs/       Analiz dokümanı ve teknik karar notları
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

API varsayılan olarak `http://localhost:8080` üzerinde ayağa kalkar.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Uygulama `http://localhost:5173` üzerinden erişilebilir.

## Kimlik Doğrulama (Auth)

Sistemde herkese açık kayıt (register) yoktur; kullanıcılar yalnızca Admin tarafından oluşturulur.

- **İlk admin:** Uygulama ilk kez ayağa kalktığında, `users` tablosu boşsa `.env`'deki
  `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD` bilgileriyle otomatik bir admin kullanıcı oluşturulur.
  **İlk girişten sonra bu şifreyi değiştirin.**
- `POST /api/auth/login` — email + şifre ile giriş, JWT token döner (herkese açık).
- `POST /api/admin/users` — yeni kullanıcı oluşturma (yalnızca ADMIN, `Authorization: Bearer <token>` gerekir).
- `GET /api/admin/users` — kullanıcı listesi (yalnızca ADMIN).
- Diğer tüm `/api/**` endpoint'leri geçerli bir JWT token gerektirir.

## Durum

🚧 Geliştirme aşamasında (MVP — 1-20. gün planı).

## Bilinen Eksikler

- Proje / Haftalık Rapor / İş Kalemi CRUD endpoint'leri henüz eklenmedi
- CTO dashboard endpoint'i henüz eklenmedi
- Frontend sayfaları (login, dashboard'lar) henüz eklenmedi
- Testler henüz eklenmedi
