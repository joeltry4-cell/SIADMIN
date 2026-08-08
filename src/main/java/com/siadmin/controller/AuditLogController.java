package com.siadmin.controller;

import com.siadmin.service.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String entitas,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dari,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate sampai,
                        Model model) {
        LocalDateTime awal = dari != null ? dari.atStartOfDay() : null;
        LocalDateTime akhir = sampai != null ? sampai.atTime(23, 59, 59) : null;

        model.addAttribute("daftarLog", auditLogService.cari(entitas, awal, akhir));
        model.addAttribute("entitas", entitas);
        model.addAttribute("dari", dari);
        model.addAttribute("sampai", sampai);
        return "audit-log/list";
    }
}
