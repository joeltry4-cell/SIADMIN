package com.siadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lembur")
@Getter
@Setter
@NoArgsConstructor
public class Lembur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "karyawan_id", nullable = false)
    private Karyawan karyawan;

    @NotNull(message = "Tanggal wajib diisi")
    @Column(nullable = false)
    private LocalDate tanggal;

    @NotNull(message = "Jam mulai wajib diisi")
    @Column(nullable = false)
    private LocalTime jamMulai;

    @NotNull(message = "Jam selesai wajib diisi")
    @Column(nullable = false)
    private LocalTime jamSelesai;

    @NotBlank(message = "Alasan wajib diisi")
    @Column(nullable = false, length = 255)
    private String alasan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLembur status = StatusLembur.PENGAJUAN;

    @Column(nullable = false)
    private LocalDate tanggalPengajuan;

    @Column(length = 255)
    private String catatanAdmin;

    public double getJumlahJam() {
        if (jamMulai == null || jamSelesai == null || !jamSelesai.isAfter(jamMulai)) {
            return 0;
        }
        return Duration.between(jamMulai, jamSelesai).toMinutes() / 60.0;
    }
}
