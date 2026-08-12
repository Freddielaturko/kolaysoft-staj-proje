package com.kolaysoft.projedurum.entity;

import com.kolaysoft.projedurum.entity.enums.GenelDurum;
import com.kolaysoft.projedurum.entity.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
    name = "weekly_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "rapor_haftasi"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "rapor_haftasi", nullable = false)
    private LocalDate raporHaftasi;

    @Column(nullable = false)
    private Integer hedeflenenIlerleme; // yuzde 0-100

    @Column(nullable = false)
    private Integer gerceklesenIlerleme; // yuzde 0-100

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenelDurum genelDurum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskSeviyesi;

    private String canliTask;

    @Column(columnDefinition = "TEXT")
    private String yapilanlar;

    @Column(columnDefinition = "TEXT")
    private String yapilacaklar;

    @Column(columnDefinition = "TEXT")
    private String riskEngelNotu;

    @Column(columnDefinition = "TEXT")
    private String genelNot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "olusturan_user_id", nullable = false)
    private User olusturanUser;
}
