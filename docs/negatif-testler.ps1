$adminEmail = Read-Host "Admin email"
$adminPassword = Read-Host "Admin sifre"

$adminBody = @{ email = $adminEmail; sifre = $adminPassword } | ConvertTo-Json
$adminLogin = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $adminBody -ContentType "application/json"
$headers = @{ Authorization = "Bearer $($adminLogin.token)" }

Write-Host "`n--- Test 1: Ayni email ile ikinci kez kullanici olusturma -> 409 bekleniyor ---"
$dupBody = @{ adSoyad = "Ayse PM Kopya"; email = "ayse.pm@kolaysoft.com.tr"; sifre = "Sifre123!"; rol = "PM" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Post -Body $dupBody -ContentType "application/json" -Headers $headers
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Test 2: CTO'yu sorumluPmId olarak atama -> 400 bekleniyor ---"
$users = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Get -Headers $headers
$ctoUser = $users | Where-Object { $_.rol -eq "CTO" } | Select-Object -First 1
$badProjectBody = @{ ad = "Gecersiz Proje"; musteri = "Test"; sorumluPmId = $ctoUser.id; durum = "AKTIF" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/projects" -Method Post -Body $badProjectBody -ContentType "application/json" -Headers $headers
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Test 3: Token olmadan istek -> 401 bekleniyor ---"
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method Get
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Test 4: Yanlis sifre ile giris -> 401 bekleniyor ---"
$wrongPassBody = @{ email = $adminEmail; sifre = "YanlisSifre999" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $wrongPassBody -ContentType "application/json"
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Test 5: 6 karakterden kisa sifre ile kullanici olusturma -> 400 bekleniyor ---"
$shortPassBody = @{ adSoyad = "Kisa Sifreli"; email = "kisa.sifre@kolaysoft.com.tr"; sifre = "123"; rol = "PM" } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Post -Body $shortPassBody -ContentType "application/json" -Headers $headers
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Test 6: Baska bir PM, sahibi olmadigi projeye yazmaya calisirsa -> 403 bekleniyor ---"
$pm2Body = @{ adSoyad = "Mehmet PM2"; email = "mehmet.pm2@kolaysoft.com.tr"; sifre = "Sifre123!"; rol = "PM" } | ConvertTo-Json
try {
    $pm2 = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Post -Body $pm2Body -ContentType "application/json" -Headers $headers
} catch {
    $pm2 = $users | Where-Object { $_.email -eq "mehmet.pm2@kolaysoft.com.tr" } | Select-Object -First 1
}

$pm2LoginBody = @{ email = "mehmet.pm2@kolaysoft.com.tr"; sifre = "Sifre123!" } | ConvertTo-Json
$pm2Login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $pm2LoginBody -ContentType "application/json"
$pm2Headers = @{ Authorization = "Bearer $($pm2Login.token)" }

$allProjects = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method Get -Headers $headers
$existingProject = $allProjects | Select-Object -First 1

$intrusionBody = @{
    raporHaftasi = "2026-08-17"
    hedeflenenIlerleme = 10
    gerceklesenIlerleme = 5
    genelDurum = "SARI"
    riskSeviyesi = "ORTA"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$($existingProject.id)/weekly-reports" -Method Post -Body $intrusionBody -ContentType "application/json" -Headers $pm2Headers
} catch {
    $_.Exception.Response.StatusCode
}

Write-Host "`n--- Tum testler tamamlandi ---"
