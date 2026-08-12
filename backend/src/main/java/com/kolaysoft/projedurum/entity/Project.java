package com.kolaysoft.projedurum.entity;

import com.kolaysoft.projedurum.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private String ad;

    private String musteri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorumlu_pm_id", nullable = false)
    private User sorumluPm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus durum;
}
