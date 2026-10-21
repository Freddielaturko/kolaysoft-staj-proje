# Deployment

Backend ve frontend ayrı platformlarda barındırılıyor (Vercel Java'yı native
desteklemediği için bu ayrım gerekti).

## Canlı Adresler

- **Frontend:** https://kolaysoft-staj-proje.vercel.app
- **Backend API:** https://kolaysoft-staj-proje.onrender.com/api

## Altyapı

- **Frontend:** Vercel (React/Vite, Root Directory: `frontend`)
- **Backend:** Render Web Service, Docker runtime (`backend/Dockerfile`)
- **Veritabanı:** Neon PostgreSQL. İlk kurulumda Render PostgreSQL kullanıyordum, ücretsiz veritabanı 90 gün sonra silindiği için Neon'a taşıdım

## Ortam Değişkenleri

**Render (backend):**

| Değişken | Açıklama |
|---|---|
| `DB_URL` | `jdbc:postgresql://<neon-host>/<db>?sslmode=require` (Neon panelindeki bağlantı bilgisinden, `postgresql://` yerine `jdbc:postgresql://` ile ve kullanıcı/şifre olmadan) |
| `DB_USERNAME`, `DB_PASSWORD` | Neon rol bilgileri |
| `JWT_SECRET` | En az 32 karakter, rastgele |
| `JWT_EXPIRATION_MS` | `86400000` |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_ADMIN_AD_SOYAD` | İlk admin seed bilgileri |
| `CORS_ALLOWED_ORIGINS` | `https://kolaysoft-staj-proje.vercel.app` |
| `APP_DEMO_ENABLED`, `APP_DEMO_PASSWORD` | Canlı demoda örnek verinin yüklenmesi için `true` ve demo şifresi |


**Vercel (frontend):**

| Değişken | Açıklama |
|---|---|
| `VITE_API_BASE_URL` | `https://kolaysoft-staj-proje.onrender.com/api` |
| `VITE_DEMO_MODE`, `VITE_DEMO_PASSWORD` | Giriş ekranındaki demo rol butonları için `true` ve backend'deki demo şifresi |

## Deploy Sürecinde Karşılaşılan Hatalar ve Çözümleri

Deploy sürecinde birden fazla gerçek hatayla karşılaştım, hepsini teker teker çözdüm:

1. **Render Dockerfile bulamıyor** (`open Dockerfile: no such file or directory`) — Root Directory ayarı ile Dockerfile Path/Build Context alanlarının aynı anda "backend/" öneki eklemesi çakışmaya sebep oluyordu. Çözüm: Root Directory'yi boşaltıp, Dockerfile Path'i `backend/Dockerfile`, Build Context'i `backend` olarak repo köküne göre ayarladım.

2. **Maven indirme 404 hatası** — Dockerfile'da Maven'i `dlcdn.apache.org`'dan indiriyordum, bu adres yalnızca en güncel sürümleri barındırıyor, 3.9.9 oradan kalkmıştı. Çözüm: kalıcı arşiv adresi `archive.apache.org`'a geçtim.

3. **Lombok annotation processing hatası** (`cannot find symbol getX()`) — JDK 23 ve sonrasında `javac`, classpath üzerinden otomatik annotation processor keşfini varsayılan olarak kapattı. Yerel makinemde bir şekilde çalışıyordu ama Render'ın temiz Docker ortamında bu hemen açığa çıktı. Çözüm: `maven-compiler-plugin`'e Lombok'u `annotationProcessorPaths` ile açıkça annotation processor olarak tanımladım.


## Doğrulama

Canlı ortamda `POST /api/auth/login` ile admin girişi test edildi, JWT token başarıyla döndü. Frontend üzerinden de tam giriş akışı (Vercel → Render API → PostgreSQL) uçtan uca doğrulandı.

## Sınırlamalar

- Render'ın ücretsiz planı 15 dakika hareketsizlikte uykuya geçiyor
- Hibernate enum kolonları için veritabanında CHECK kısıtı oluşturuyor ve `ddl-auto: update` bu kısıtı sonradan güncellemiyor. `LEAD` rolü ve `ARSIV` durumu eklendiği için eski bir veritabanında `users_rol_check` ve `projects_durum_check` kısıtlarının silinmesi gerekir. Neon'da sıfırdan kurulan veritabanında bu sorun yok
