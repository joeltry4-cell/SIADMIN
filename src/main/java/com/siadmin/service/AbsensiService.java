package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusAbsensi;
import com.siadmin.repository.AbsensiRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AbsensiService {

    private final AbsensiRepository absensiRepository;
    private final AuditLogService auditLogService;

    public AbsensiService(AbsensiRepository absensiRepository, AuditLogService auditLogService) {
        this.absensiRepository = absensiRepository;
        this.auditLogService = auditLogService;
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
        boolean isNew = absensi.getId() == null;
        Absensi saved = absensiRepository.save(absensi);
        auditLogService.log(AuditLogService.currentUsername(), isNew ? AksiAudit.CREATE : AksiAudit.UPDATE,
                "Absensi", saved.getId(),
                "Absensi " + saved.getKaryawan().getNamaLengkap() + " tanggal " + saved.getTanggal()
                        + " status " + saved.getStatus());
        return saved;
    }

    public void deleteById(Long id) {
        Absensi absensi = findById(id);
        absensiRepository.deleteById(id);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.DELETE, "Absensi", id,
                "Absensi " + absensi.getKaryawan().getNamaLengkap() + " tanggal " + absensi.getTanggal() + " dihapus");
    }

    /**
     * Dipakai khusus oleh persetujuan cuti: create-or-overwrite baris absensi untuk satu tanggal,
     * tanpa mencatat audit log per baris (approval multi-hari cukup 1 entry APPROVE di CutiService).
     */
    public void upsertUntukCuti(Karyawan karyawan, LocalDate tanggal, StatusAbsensi status, String keterangan) {
        Absensi absensi = findByKaryawanAndTanggal(karyawan, tanggal).orElseGet(Absensi::new);
        absensi.setKaryawan(karyawan);
        absensi.setTanggal(tanggal);
        absensi.setStatus(status);
        absensi.setJamMasuk(null);
        absensi.setJamKeluar(null);
        absensi.setKeterangan(keterangan);
        absensiRepository.save(absensi);
    }
}
