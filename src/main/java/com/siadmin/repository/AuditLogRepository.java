package com.siadmin.repository;

import com.siadmin.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByWaktuDesc();

    @Query("select a from AuditLog a where " +
           "(:entitas is null or a.entitas = :entitas) and " +
           "(:awal is null or a.waktu >= :awal) and " +
           "(:akhir is null or a.waktu <= :akhir) " +
           "order by a.waktu desc")
    List<AuditLog> cari(@Param("entitas") String entitas,
                         @Param("awal") LocalDateTime awal,
                         @Param("akhir") LocalDateTime akhir);
}
