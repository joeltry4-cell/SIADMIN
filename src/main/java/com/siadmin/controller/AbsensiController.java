package com.siadmin.controller;

import com.siadmin.model.Absensi;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusAbsensi;
import com.siadmin.security.UserPrincipal;
import com.siadmin.service.AbsensiService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/absensi")
public class AbsensiController {

    private final AbsensiService absensiService;

    public AbsensiController(AbsensiService absensiService) {
        this.absensiService = absensiService;
    }

    @GetMapping
    public String absensiSaya(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        Karyawan karyawan = principal.getUser().getKaryawan();
        model.addAttribute("karyawan", karyawan);

        if (karyawan != null) {
            model.addAttribute("daftarAbsensi", absensiService.findByKaryawan(karyawan));
            model.addAttribute("sudahAbsenHariIni", absensiService.findByKaryawanAndTanggal(karyawan, LocalDate.now()).isPresent());
        } else {
            model.addAttribute("daftarAbsensi", List.of());
            model.addAttribute("sudahAbsenHariIni", false);
        }
        return "absensi/list";
    }

    @GetMapping("/tambah")
    public String formTambah(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal.getUser().getKaryawan() == null) {
            return "redirect:/absensi";
        }
        Absensi absensi = new Absensi();
        absensi.setTanggal(LocalDate.now());
        model.addAttribute("absensi", absensi);
        model.addAttribute("statusList", StatusAbsensi.values());
        return "absensi/form";
    }

    @PostMapping("/simpan")
    public String simpan(@AuthenticationPrincipal UserPrincipal principal,
                          @Valid @ModelAttribute("absensi") Absensi absensi,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        Karyawan karyawan = principal.getUser().getKaryawan();
        if (karyawan == null) {
            return "redirect:/absensi";
        }

        if (absensiService.sudahAbsen(karyawan, absensi.getTanggal(), null)) {
            result.rejectValue("tanggal", "duplicate", "Anda sudah mengisi absensi untuk tanggal ini");
        }

        if (result.hasErrors()) {
            model.addAttribute("statusList", StatusAbsensi.values());
            return "absensi/form";
        }

        absensi.setKaryawan(karyawan);
        absensiService.save(absensi);
        redirectAttributes.addFlashAttribute("pesan", "Absensi berhasil disimpan");
        return "redirect:/absensi";
    }

    @GetMapping("/rekap")
    public String rekap(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggal,
                         Model model) {
        LocalDate tanggalDipilih = tanggal != null ? tanggal : LocalDate.now();
        model.addAttribute("tanggal", tanggalDipilih);
        model.addAttribute("daftarAbsensi", absensiService.findByTanggal(tanggalDipilih));
        return "absensi/rekap";
    }

    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        absensiService.deleteById(id);
        redirectAttributes.addFlashAttribute("pesan", "Data absensi berhasil dihapus");
        return "redirect:/absensi/rekap";
    }
}
