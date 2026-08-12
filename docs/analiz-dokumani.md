# Analiz Dokümanı — Haftalık Proje Durum Raporlama ve CTO Takip Sistemi

## 1. Problem

Kolaysoft'ta proje durumları şu anda manuel olarak PowerPoint üzerinden raporlanıyor: önce portföyün genel özeti, ardından her proje için ayrı bir durum sayfası hazırlanıyor. Bu süreç manuel, dağınık ve geçmişe dönük sorgulanabilir değil.

Bu sistem, bu manuel raporlama akışını dijitalleştirir: proje yöneticileri haftalık ilerlemelerini sisteme girer, CTO tüm projeleri tek bir yerden, filtreleyerek ve geçmişe dönük olarak izleyebilir.

## 2. Roller ve Yetkiler

| Rol | Yetkiler |
|---|---|
| **Admin** | Kullanıcı oluşturma/düzenleme, rol atama, proje tanımlama, durum/risk seçeneklerini yönetme |
| **Proje Yöneticisi (PM)** | Yalnızca kendisine atanmış proje(ler) için haftalık rapor oluşturma/güncelleme, iş kalemi (task) ekleme/güncelleme |
| **CTO** | Tüm projeleri portföy özetinde görme, proje/hafta/durum/risk filtreleme, proje detayına inme |

Yetkilendirme kuralı: PM yalnızca kendi projelerine yazabilir; başka PM'in projesine erişemez. CTO her şeyi okuyabilir ama düzenleyemez. Admin sistem verisini yönetir.

## 3. Kapsam (MVP)

**Dahil:**
- Kullanıcı girişi (auth) + rol bazlı yetkilendirme (JWT)
- Proje yönetimi (CRUD — admin)
- Haftalık rapor girişi/güncelleme (PM)
- İş kalemleri (task) yönetimi
- CTO dashboard (portföy özeti + filtreleme)
- Validasyon ve anlamlı hata mesajları

**Kapsam Dışı (MVP sonrası genişletme):**
- Jira/Azure DevOps entegrasyonu
- Gerçek bildirim altyapısı (email/push)
- Gelişmiş BI / otomatik veri çekme
- Audit log, monitoring
- PDF/Excel çıktısı
- AI özellikleri

## 4. Kullanıcı Akışları

### Akış 1 — PM haftalık rapor oluşturur
1. PM giriş yapar (JWT ile authenticate olur)
2. Kendisine atanmış proje(ler) listesinden birini seçer
3. "Yeni Haftalık Rapor" formunu açar; rapor haftası/tarihi otomatik önerilir
4. Hedeflenen ilerleme (%), gerçekleşen ilerleme (%), genel durum, takvim durumu, risk seviyesi, canlı task bilgisini girer
5. O hafta yapılanlar / gelecek hafta yapılacaklar / risk-engel notu / genel not alanlarını doldurur
6. Kaydeder → validasyon geçerse rapor oluşur, geçmezse alan bazlı hata gösterilir

### Akış 2 — PM iş kalemi (task) ekler/günceller
1. PM proje detayına girer
2. İş kalemi ekler: başlık, açıklama, sorumlu, durum, planlanan/tamamlanan tarih, not
3. Var olan bir task'ın durumunu günceller (örn: Beklemede → Devam Ediyor → Tamamlandı)

### Akış 3 — CTO portföy takibi yapar
1. CTO giriş yapar
2. Dashboard'da tüm projelerin son haftalık durumunu (durum/risk renk kodlu) tek görünümde görür
3. Proje / hafta / durum / risk filtrelerini uygular
4. Bir projeye tıklayıp detayına iner: geçmiş haftalık raporları, iş kalemlerini ve risk notlarını inceler

### Akış 4 — Admin sistem verisini yönetir
1. Admin giriş yapar
2. Kullanıcı oluşturur, rol atar (PM/CTO)
3. Proje tanımlar: proje adı, müşteri/demo müşteri, sorumlu PM, başlangıç durumu
4. Durum/risk seçeneklerini (örn: Yeşil/Sarı/Kırmızı) tanımlar

## 5. Veri Modeli (Öneri — Entity Taslağı)

- **User**: id, ad, email, şifre(hash), rol (ADMIN / PM / CTO)
- **Project**: id, ad, müşteri/demo müşteri, sorumlu PM (User FK), durum
- **WeeklyReport**: id, project (FK), raporHaftasi/tarih, hedeflenenIlerleme, gerceklesenIlerleme, genelDurum, takvimDurumu, riskSeviyesi, canliTask, yapilanlar, yapilacaklar, riskEngelNotu, genelNot, oluşturanUser (FK)
- **TaskItem**: id, project (FK), başlık, açıklama, sorumlu, durum, planlananTarih, tamamlananTarih, not

İlişkiler: `User 1—N Project` (PM sorumluluğu), `Project 1—N WeeklyReport`, `Project 1—N TaskItem`.

## 6. Kabul Kriterleri

- PM yalnızca kendi projelerinde haftalık rapor oluşturabilir/güncelleyebilir; başka PM'in projesine yazma denemesi 403 döner.
- CTO herhangi bir projeye/rapor'a yazma isteği gönderirse 403 döner.
- Zorunlu alanlar boş bırakılırsa anlamlı hata mesajıyla reddedilir (örn: "Hedeflenen ilerleme boş bırakılamaz").
- CTO dashboard filtreleri (proje/hafta/durum/risk) doğru sonucu döndürür ve birlikte kullanılabilir (AND mantığı).
- Aynı proje + aynı hafta için ikinci bir rapor oluşturulmaya çalışılırsa ya güncelleme olarak ele alınır ya da anlamlı hata verilir (karar: güncelleme — upsert mantığı).
- Yetkisiz kullanıcı (token yok/geçersiz) korumalı endpoint'lere erişemez, 401 döner.

## 7. Açık Sorular

- Aynı proje için birden fazla PM olabilir mi, yoksa 1 proje = 1 sorumlu PM mi? (Şimdilik: 1 proje = 1 sorumlu PM, MVP basitliği için)
- Ekip lideri rolü ileride eklenir mi? (Şimdilik MVP dışı, opsiyonel genişletme)
- Durum/risk seçenekleri sabit enum mu olacak, admin tarafından yönetilebilir dinamik liste mi? (Şimdilik: sabit enum, MVP sonrası dinamikleştirilebilir)
