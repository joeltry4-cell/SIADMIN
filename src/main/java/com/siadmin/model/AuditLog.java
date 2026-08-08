package com.siadmin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AksiAudit aksi;

    @Column(nullable = false, length = 50)
    private String entitas;

    @Column(nullable = false)
    private Long entitasId;

    @Column(length = 500)
    private String keterangan;

    @Column(nullable = false)
    private LocalDateTime waktu;
}
