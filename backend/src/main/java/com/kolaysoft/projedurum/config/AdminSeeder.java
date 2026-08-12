package com.kolaysoft.projedurum.config;

import com.kolaysoft.projedurum.entity.User;
import com.kolaysoft.projedurum.entity.enums.Role;
import com.kolaysoft.projedurum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Sistemde hic kullanici yoksa, .env / ortam degiskenlerinden okunan bilgilerle
// ilk ADMIN kullaniciyi otomatik olusturur. Boylece "admin kullanici olusturur"
// akisinin ilk adimi (tavuk-yumurta sorunu) cozulur.
// Sadece users tablosu bossa calisir; sonraki ayaga kalkislarda tekrar olusturmaz.
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@kolaysoft.com.tr}")
    private String adminEmail;

    @Value("${app.admin.password:ChangeMe123!}")
    private String adminPassword;

    @Value("${app.admin.ad-soyad:Sistem Admin}")
    private String adminAdSoyad;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setAdSoyad(adminAdSoyad);
        admin.setEmail(adminEmail);
        admin.setSifreHash(passwordEncoder.encode(adminPassword));
        admin.setRol(Role.ADMIN);
        userRepository.save(admin);

        log.info("Ilk admin kullanici olusturuldu: {}", adminEmail);
        log.warn("Guvenlik: ilk giristen sonra APP_ADMIN_PASSWORD ile verilen sifreyi degistirin.");
    }
}
