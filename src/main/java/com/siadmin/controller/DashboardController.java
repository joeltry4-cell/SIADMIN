package com.siadmin.controller;

import com.siadmin.model.Absensi;
import com.siadmin.model.StatusAbsensi;
import com.siadmin.security.UserPrincipal;
import com.siadmin.service.AbsensiService;
import com.siadmin.service.KaryawanService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final KaryawanService karyawanService;
    private final AbsensiService absensiService;

    public DashboardController(KaryawanService karyawanService, AbsensiService absensiService) {
        this.karyawanService = karyawanService;
        this.absensiService = absensiService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        boolean isAdmin = principal.getUser().getRole().name().equals("ADMIN");
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            List<Absensi> absensiHariIni = absensiService.findByTanggal(LocalDate.now());
            model.addAttribute("totalKaryawan", karyawanService.count());
            model.addAttribute("hadir", hitung(absensiHariIni, StatusAbsensi.HADIR));
            model.addAttribute("izin", hitung(absensiHariIni, StatusAbsensi.IZIN) + hitung(absensiHariIni, StatusAbsensi.SAKIT) + hitung(absensiHariIni, StatusAbsensi.CUTI));
            model.addAttribute("belumAbsen", karyawanService.count() - absensiHariIni.size());
        } else {
            var karyawan = principal.getUser().getKaryawan();
            model.addAttribute("karyawan", karyawan);
            model.addAttribute("sudahAbsenHariIni",
                    karyawan != null && absensiService.findByKaryawanAndTanggal(karyawan, LocalDate.now()).isPresent());
        }

        return "dashboard";
    }

    private long hitung(List<Absensi> daftar, StatusAbsensi status) {
        return daftar.stream().filter(a -> a.getStatus() == status).count();
    }
}
