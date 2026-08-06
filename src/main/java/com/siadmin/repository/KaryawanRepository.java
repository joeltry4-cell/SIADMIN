package com.siadmin.repository;

import com.siadmin.model.Karyawan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KaryawanRepository extends JpaRepository<Karyawan, Long> {
    Optional<Karyawan> findByNip(String nip);
    boolean existsByNip(String nip);
    java.util.List<Karyawan> findByNamaLengkapContainingIgnoreCaseOrNipContainingIgnoreCase(String nama, String nip);
}
