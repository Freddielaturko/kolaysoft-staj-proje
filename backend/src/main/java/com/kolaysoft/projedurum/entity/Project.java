package com.kolaysoft.projedurum.entity;

import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ornek: KLF-241. Eski kayitlarda bos olabilir, response tarafinda id'den uretiyorum
    @Column(unique = true)
    private String kod;

    @Column(nullable = false)
    private String ad;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    private String musteri;

    private String departman;

    // TL cinsinden, kurus takibi gerekmedigi icin tam sayi
    private Long butce;

    private LocalDate teslimTarihi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorumlu_pm_id", nullable = false)
    private User sorumluPm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ekip_lideri_id")
    private User ekipLideri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus durum;
}
