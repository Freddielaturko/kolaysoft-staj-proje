package com.kolaysoft.projedurum.entity;

import com.kolaysoft.projedurum.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adSoyad;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String sifreHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role rol;
}
