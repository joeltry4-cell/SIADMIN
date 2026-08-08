package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.Karyawan;
import com.siadmin.repository.KaryawanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KaryawanService {

    private final KaryawanRepository karyawanRepository;
    private final AuditLogService auditLogService;

    public KaryawanService(KaryawanRepository karyawanRepository, AuditLogService auditLogService) {
        this.karyawanRepository = karyawanRepository;
        this.auditLogService = auditLogService;
    }

    public List<Karyawan> findAll() {
        return karyawanRepository.findAll();
    }

    public List<Karyawan> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return karyawanRepository.findByNamaLengkapContainingIgnoreCaseOrNipContainingIgnoreCase(keyword, keyword);
    }

    public Karyawan findById(Long id) {
        return karyawanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Karyawan dengan id " + id + " tidak ditemukan"));
    }

    public boolean isNipDuplicate(String nip, Long excludeId) {
        return karyawanRepository.findByNip(nip)
                .map(k -> !k.getId().equals(excludeId))
                .orElse(false);
    }

    public Karyawan save(Karyawan karyawan) {
        boolean isNew = karyawan.getId() == null;
        Karyawan saved = karyawanRepository.save(karyawan);
        auditLogService.log(AuditLogService.currentUsername(), isNew ? AksiAudit.CREATE : AksiAudit.UPDATE,
                "Karyawan", saved.getId(),
                "Data karyawan " + saved.getNamaLengkap() + " (" + saved.getNip() + ") "
                        + (isNew ? "ditambahkan" : "diperbarui"));
        return saved;
    }

    public void deleteById(Long id) {
        Karyawan karyawan = findById(id);
        karyawanRepository.deleteById(id);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.DELETE, "Karyawan", id,
                "Data karyawan " + karyawan.getNamaLengkap() + " (" + karyawan.getNip() + ") dihapus");
    }

    public long count() {
        return karyawanRepository.count();
    }
}
