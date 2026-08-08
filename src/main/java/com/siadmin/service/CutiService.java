package com.siadmin.service;

import com.siadmin.model.*;
import com.siadmin.repository.CutiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CutiService {

    private final CutiRepository cutiRepository;
    private final AbsensiService absensiService;
    private final AuditLogService auditLogService;

    public CutiService(CutiRepository cutiRepository, AbsensiService absensiService, AuditLogService auditLogService) {
        this.cutiRepository = cutiRepository;
        this.absensiService = absensiService;
        this.auditLogService = auditLogService;
    }

    public List<Cuti> findByKaryawan(Karyawan karyawan) {
        return cutiRepository.findByKaryawanOrderByTanggalPengajuanDescIdDesc(karyawan);
    }

    public List<Cuti> findAll() {
        return cutiRepository.findAllOrderByTanggalPengajuanDesc();
    }

    public Cuti findById(Long id) {
        return cutiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Data cuti dengan id " + id + " tidak ditemukan"));
    }

    public boolean isRentangValid(LocalDate mulai, LocalDate selesai) {
        return mulai != null && selesai != null && !selesai.isBefore(mulai);
    }

    public Cuti ajukan(Cuti cuti, Karyawan karyawan) {
        cuti.setKaryawan(karyawan);
        cuti.setStatus(StatusCuti.PENGAJUAN);
        cuti.setTanggalPengajuan(LocalDate.now());
        cuti.setCatatanAdmin(null);
        Cuti saved = cutiRepository.save(cuti);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.CREATE, "Cuti", saved.getId(),
                "Pengajuan cuti " + karyawan.getNamaLengkap() + " (" + saved.getJenis() + ") "
                        + saved.getTanggalMulai() + " s/d " + saved.getTanggalSelesai());
        return saved;
    }

    @Transactional
    public void setujui(Long id) {
        Cuti cuti = findById(id);
        if (cuti.getStatus() != StatusCuti.PENGAJUAN) {
            throw new IllegalStateException("Pengajuan cuti ini sudah diproses sebelumnya");
        }

        cuti.setStatus(StatusCuti.DISETUJUI);
        cuti.setNotifikasiDibaca(false);
        cutiRepository.save(cuti);

        StatusAbsensi statusAbsensi = StatusAbsensi.valueOf(cuti.getJenis().name());
        long jumlahHari = 0;
        for (LocalDate tgl = cuti.getTanggalMulai(); !tgl.isAfter(cuti.getTanggalSelesai()); tgl = tgl.plusDays(1)) {
            absensiService.upsertUntukCuti(cuti.getKaryawan(), tgl, statusAbsensi,
                    "Otomatis dari pengajuan cuti #" + cuti.getId());
            jumlahHari++;
        }

        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.APPROVE, "Cuti", cuti.getId(),
                "Cuti " + cuti.getKaryawan().getNamaLengkap() + " disetujui, " + jumlahHari
                        + " hari absensi otomatis diperbarui");
    }

    @Transactional
    public void tolak(Long id, String catatanAdmin) {
        Cuti cuti = findById(id);
        if (cuti.getStatus() != StatusCuti.PENGAJUAN) {
            throw new IllegalStateException("Pengajuan cuti ini sudah diproses sebelumnya");
        }

        cuti.setStatus(StatusCuti.DITOLAK);
        cuti.setCatatanAdmin(catatanAdmin);
        cuti.setNotifikasiDibaca(false);
        cutiRepository.save(cuti);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.REJECT, "Cuti", cuti.getId(),
                "Cuti " + cuti.getKaryawan().getNamaLengkap() + " ditolak: " + catatanAdmin);
    }

    public long hitungBelumDibaca(Karyawan karyawan) {
        return cutiRepository.countByKaryawanAndNotifikasiDibacaFalse(karyawan);
    }

    public void tandaiSudahDibaca(Karyawan karyawan) {
        List<Cuti> belumDibaca = cutiRepository.findByKaryawanAndNotifikasiDibacaFalse(karyawan);
        belumDibaca.forEach(c -> c.setNotifikasiDibaca(true));
        cutiRepository.saveAll(belumDibaca);
    }
}
