package com.siadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "karyawan")
@Getter
@Setter
@NoArgsConstructor
public class Karyawan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "NIP wajib diisi")
    @Column(nullable = false, unique = true, length = 30)
    private String nip;

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Column(nullable = false, length = 150)
    private String namaLengkap;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Jenis kelamin wajib dipilih")
    @Column(nullable = false, length = 20)
    private JenisKelamin jenisKelamin;

    private String tempatLahir;

    private LocalDate tanggalLahir;

    @Column(length = 255)
    private String alamat;

    @Column(length = 20)
    private String noTelepon;

    @Email(message = "Format email tidak valid")
    @Column(length = 150)
    private String email;

    @NotBlank(message = "Departemen wajib diisi")
    @Column(nullable = false, length = 100)
    private String departemen;

    @NotBlank(message = "Jabatan wajib diisi")
    @Column(nullable = false, length = 100)
    private String jabatan;

    @NotNull(message = "Tanggal masuk wajib diisi")
    private LocalDate tanggalMasuk;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status kepegawaian wajib dipilih")
    @Column(nullable = false, length = 20)
    private StatusKepegawaian statusKepegawaian;

    @Column(nullable = false)
    private boolean aktif = true;

    @Column(length = 50)
    private String tim;

    @Column(length = 50)
    private String shift;

    private Integer siklusCutiBulan;

    private LocalDate tanggalMulaiSiklusCuti;
}
