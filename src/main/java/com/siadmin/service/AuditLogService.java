package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.AuditLog;
import com.siadmin.repository.AuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public static String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public void log(String username, AksiAudit aksi, String entitas, Long entitasId, String keterangan) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAksi(aksi);
        log.setEntitas(entitas);
        log.setEntitasId(entitasId);
        log.setKeterangan(keterangan);
        log.setWaktu(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByWaktuDesc();
    }

    public List<AuditLog> cari(String entitas, LocalDateTime awal, LocalDateTime akhir) {
        return auditLogRepository.cari((entitas == null || entitas.isBlank()) ? null : entitas, awal, akhir);
    }
}
