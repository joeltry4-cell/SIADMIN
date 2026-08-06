package com.siadmin.repository;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbsensiRepository extends JpaRepository<Absensi, Long> {

    @Query("select a from Absensi a join fetch a.karyawan k where a.tanggal = :tanggal order by k.namaLengkap asc")
    List<Absensi> findByTanggalOrderByKaryawanNamaLengkapAsc(LocalDate tanggal);

    List<Absensi> findByKaryawanOrderByTanggalDesc(Karyawan karyawan);
    List<Absensi> findByKaryawanIdOrderByTanggalDesc(Long karyawanId);
    Optional<Absensi> findByKaryawanAndTanggal(Karyawan karyawan, LocalDate tanggal);

    @Query("select a from Absensi a join fetch a.karyawan where a.tanggal between :start and :end order by a.tanggal desc")
    List<Absensi> findByTanggalBetweenOrderByTanggalDesc(LocalDate start, LocalDate end);
}
