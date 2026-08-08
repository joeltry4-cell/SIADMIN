package com.siadmin.repository;

import com.siadmin.model.Cuti;
import com.siadmin.model.Karyawan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CutiRepository extends JpaRepository<Cuti, Long> {

    @Query("select c from Cuti c left join fetch c.disetujuiOleh where c.karyawan = :karyawan " +
           "order by c.tanggalPengajuan desc, c.id desc")
    List<Cuti> findByKaryawanOrderByTanggalPengajuanDescIdDesc(Karyawan karyawan);

    long countByKaryawanAndNotifikasiDibacaFalse(Karyawan karyawan);

    List<Cuti> findByKaryawanAndNotifikasiDibacaFalse(Karyawan karyawan);

    @Query("select c from Cuti c join fetch c.karyawan left join fetch c.disetujuiOleh " +
           "order by c.tanggalPengajuan desc, c.id desc")
    List<Cuti> findAllOrderByTanggalPengajuanDesc();
}
