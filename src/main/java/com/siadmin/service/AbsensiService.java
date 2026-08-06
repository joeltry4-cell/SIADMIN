package com.siadmin.service;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.repository.AbsensiRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AbsensiService {

    private final AbsensiRepository absensiRepository;

    public AbsensiService(AbsensiRepository absensiRepository) {
        this.absensiRepository = absensiRepository;
    }

    public List<Absensi> findByKaryawan(Karyawan karyawan) {
        return absensiRepository.findByKaryawanOrderByTanggalDesc(karyawan);
    }

    public List<Absensi> findByTanggal(LocalDate tanggal) {
        return absensiRepository.findByTanggalOrderByKaryawanNamaLengkapAsc(tanggal);
    }

    public List<Absensi> findByRentang(LocalDate awal, LocalDate akhir) {
        return absensiRepository.findByTanggalBetweenOrderByTanggalDesc(awal, akhir);
    }

    public List<Absensi> findAll() {
        return absensiRepository.findAll();
    }

    public Absensi findById(Long id) {
        return absensiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data absensi tidak ditemukan"));
    }

    public Optional<Absensi> findByKaryawanAndTanggal(Karyawan karyawan, LocalDate tanggal) {
        return absensiRepository.findByKaryawanAndTanggal(karyawan, tanggal);
    }

    public boolean sudahAbsen(Karyawan karyawan, LocalDate tanggal, Long excludeId) {
        return findByKaryawanAndTanggal(karyawan, tanggal)
                .map(a -> excludeId == null || !a.getId().equals(excludeId))
                .orElse(false);
    }

    public Absensi save(Absensi absensi) {
        return absensiRepository.save(absensi);
    }

    public void deleteById(Long id) {
        absensiRepository.deleteById(id);
    }
}
