package com.kolaysoft.projedurum.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Render'in ucretsiz plani 15 dakika istek gelmezse servisi uyutuyor. GitHub Actions bu
// endpoint'e duzenli istek atarak servisi uyanik tutuyor. Veritabanina dokunmuyor ki
// Neon'un compute'u da gereksiz yere surekli acik kalmasin.
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
