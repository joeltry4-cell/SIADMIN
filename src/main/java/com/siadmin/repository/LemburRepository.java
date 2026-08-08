package com.siadmin.repository;

import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import com.siadmin.model.StatusLembur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LemburRepository extends JpaRepository<Lembur, Long> {

    List<Lembur> findByKaryawanOrderByTanggalPengajuanDescIdDesc(Karyawan karyawan);

    @Query("select l from Lembur l join fetch l.karyawan order by l.tanggalPengajuan desc, l.id desc")
    List<Lembur> findAllOrderByTanggalPengajuanDesc();

    List<Lembur> findByKaryawanAndTanggalBetweenAndStatus(Karyawan karyawan, LocalDate awal, LocalDate akhir, StatusLembur status);

    List<Lembur> findByTanggalBetweenAndStatus(LocalDate awal, LocalDate akhir, StatusLembur status);
}
