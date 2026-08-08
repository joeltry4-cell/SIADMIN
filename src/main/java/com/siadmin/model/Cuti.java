package com.siadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cuti")
@Getter
@Setter
@NoArgsConstructor
public class Cuti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "karyawan_id", nullable = false)
    private Karyawan karyawan;

    @NotNull(message = "Tanggal mulai wajib diisi")
    @Column(nullable = false)
    private LocalDate tanggalMulai;

    @NotNull(message = "Tanggal selesai wajib diisi")
    @Column(nullable = false)
    private LocalDate tanggalSelesai;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Jenis cuti wajib dipilih")
    @Column(nullable = false, length = 20)
    private JenisCuti jenis;

    @NotBlank(message = "Alasan wajib diisi")
    @Column(nullable = false, length = 255)
    private String alasan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCuti status = StatusCuti.PENGAJUAN;

    @Column(nullable = false)
    private LocalDate tanggalPengajuan;

    @Column(length = 255)
    private String catatanAdmin;
}
