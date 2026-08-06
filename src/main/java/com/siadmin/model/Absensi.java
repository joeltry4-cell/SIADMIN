package com.siadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "absensi", uniqueConstraints = @UniqueConstraint(columnNames = {"karyawan_id", "tanggal"}))
@Getter
@Setter
@NoArgsConstructor
public class Absensi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "karyawan_id", nullable = false)
    private Karyawan karyawan;

    @NotNull(message = "Tanggal wajib diisi")
    @Column(nullable = false)
    private LocalDate tanggal;

    private LocalTime jamMasuk;

    private LocalTime jamKeluar;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status wajib dipilih")
    @Column(nullable = false, length = 20)
    private StatusAbsensi status;

    @Column(length = 255)
    private String keterangan;
}
