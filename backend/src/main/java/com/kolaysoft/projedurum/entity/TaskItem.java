package com.kolaysoft.projedurum.entity;

import com.kolaysoft.projedurum.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "task_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String baslik;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    private String sorumlu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus durum;

    private LocalDate planlananTarih;

    private LocalDate tamamlananTarih;

    @Column(columnDefinition = "TEXT")
    private String not;
}
