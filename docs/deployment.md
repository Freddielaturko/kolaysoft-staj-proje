# Deployment

Backend ve frontend ayrı platformlarda barındırılıyor (Vercel Java'yı native
desteklemediği için bu ayrım gerekti).

## Canlı Adresler

- **Frontend:** https://kolaysoft-staj-proje.vercel.app
- **Backend API:** https://kolaysoft-staj-proje.onrender.com/api

## Altyapı

- **Frontend:** Vercel (React/Vite, Root Directory: `frontend`)
- **Backend:** Render Web Service, Docker runtime (`backend/Dockerfile`)
- **Veritabanı:** Render PostgreSQL (Free tier — not: ücretsiz veritabanı 90 gün sonra siliniyor)

## Ortam Değişkenleri

**Render (backend):**

| Değişken | Açıklama |
|---|---|
| `DB_URL` | `jdbc:postgresql://<render-db-host>:5432/projedurum?sslmode=require` |
| `DB_USERNAME`, `DB_PASSWORD` | Render PostgreSQL bağlantı bilgileri |
| `JWT_SECRET` | En az 32 karakter, rastgele |
| `JWT_EXPIRATION_MS` | `86400000` |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_ADMIN_AD_SOYAD` | İlk admin seed bilgileri |


**Vercel (frontend):**

| Değişken | Açıklama |
|---|---|
| `VITE_API_BASE_URL` | `https://kolaysoft-staj-proje.onrender.com/api` |

## Deploy Sürecinde Karşılaşılan Hatalar ve Çözümleri

Deploy sürecinde birden fazla gerçek hatayla karşılaştım, hepsini teker teker çözdüm:

1. **Render Dockerfile bulamıyor** (`open Dockerfile: no such file or directory`) — Root Directory ayarı ile Dockerfile Path/Build Context alanlarının aynı anda "backend/" öneki eklemesi çakışmaya sebep oluyordu. Çözüm: Root Directory'yi boşaltıp, Dockerfile Path'i `backend/Dockerfile`, Build Context'i `backend` olarak repo köküne göre ayarladım.

2. **Maven indirme 404 hatası** — Dockerfile'da Maven'i `dlcdn.apache.org`'dan indiriyordum, bu adres yalnızca en güncel sürümleri barındırıyor, 3.9.9 oradan kalkmıştı. Çözüm: kalıcı arşiv adresi `archive.apache.org`'a geçtim.

3. **Lombok annotation processing hatası** (`cannot find symbol getX()`) — JDK 23 ve sonrasında `javac`, classpath üzerinden otomatik annotation processor keşfini varsayılan olarak kapattı. Yerel makinemde bir şekilde çalışıyordu ama Render'ın temiz Docker ortamında bu hemen açığa çıktı. Çözüm: `maven-compiler-plugin`'e Lombok'u `annotationProcessorPaths` ile açıkça annotation processor olarak tanımladım.


## Doğrulama

Canlı ortamda `POST /api/auth/login` ile admin girişi test edildi, JWT token başarıyla döndü. Frontend üzerinden de tam giriş akışı (Vercel → Render API → PostgreSQL) uçtan uca doğrulandı.

## Sınırlamalar

- Render'ın ücretsiz planı 15 dakika hareketsizlikte uykuya geçiyor
- Ücretsiz PostgreSQL 90 gün sonra otomatik siliniyor
