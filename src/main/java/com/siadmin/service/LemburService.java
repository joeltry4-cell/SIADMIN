package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import com.siadmin.model.StatusLembur;
import com.siadmin.repository.LemburRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class LemburService {

    private final LemburRepository lemburRepository;
    private final AuditLogService auditLogService;

    public LemburService(LemburRepository lemburRepository, AuditLogService auditLogService) {
        this.lemburRepository = lemburRepository;
        this.auditLogService = auditLogService;
    }

    public List<Lembur> findByKaryawan(Karyawan karyawan) {
        return lemburRepository.findByKaryawanOrderByTanggalPengajuanDescIdDesc(karyawan);
    }

    public List<Lembur> findAll() {
        return lemburRepository.findAllOrderByTanggalPengajuanDesc();
    }

    public List<Lembur> findApprovedByRentang(LocalDate awal, LocalDate akhir) {
        return lemburRepository.findByTanggalBetweenAndStatus(awal, akhir, StatusLembur.DISETUJUI);
    }

    public List<Lembur> findApprovedByKaryawanAndBulan(Karyawan karyawan, YearMonth bulan) {
        return lemburRepository.findByKaryawanAndTanggalBetweenAndStatus(
                karyawan, bulan.atDay(1), bulan.atEndOfMonth(), StatusLembur.DISETUJUI);
    }

    public Lembur findById(Long id) {
        return lemburRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data lembur dengan id " + id + " tidak ditemukan"));
    }

    public boolean isJamValid(LocalTime mulai, LocalTime selesai) {
        return mulai != null && selesai != null && selesai.isAfter(mulai);
    }

    public Lembur ajukan(Lembur lembur, Karyawan karyawan) {
        lembur.setKaryawan(karyawan);
        lembur.setStatus(StatusLembur.PENGAJUAN);
        lembur.setTanggalPengajuan(LocalDate.now());
        lembur.setCatatanAdmin(null);
        Lembur saved = lemburRepository.save(lembur);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.CREATE, "Lembur", saved.getId(),
                "Pengajuan lembur " + karyawan.getNamaLengkap() + " tanggal " + saved.getTanggal()
                        + " (" + saved.getJumlahJam() + " jam)");
        return saved;
    }

    @Transactional
    public void setujui(Long id) {
        Lembur lembur = findById(id);
        if (lembur.getStatus() != StatusLembur.PENGAJUAN) {
            throw new IllegalStateException("Pengajuan lembur ini sudah diproses sebelumnya");
        }

        lembur.setStatus(StatusLembur.DISETUJUI);
        lemburRepository.save(lembur);

        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.APPROVE, "Lembur", lembur.getId(),
                "Lembur " + lembur.getKaryawan().getNamaLengkap() + " tanggal " + lembur.getTanggal() + " disetujui");
    }

    @Transactional
    public void tolak(Long id, String catatanAdmin) {
        Lembur lembur = findById(id);
        if (lembur.getStatus() != StatusLembur.PENGAJUAN) {
            throw new IllegalStateException("Pengajuan lembur ini sudah diproses sebelumnya");
        }

        lembur.setStatus(StatusLembur.DITOLAK);
        lembur.setCatatanAdmin(catatanAdmin);
        lemburRepository.save(lembur);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.REJECT, "Lembur", lembur.getId(),
                "Lembur " + lembur.getKaryawan().getNamaLengkap() + " tanggal " + lembur.getTanggal()
                        + " ditolak: " + catatanAdmin);
    }
}
