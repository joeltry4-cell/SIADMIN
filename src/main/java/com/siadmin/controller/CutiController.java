package com.siadmin.controller;

import com.siadmin.model.Cuti;
import com.siadmin.model.JenisCuti;
import com.siadmin.model.Karyawan;
import com.siadmin.security.UserPrincipal;
import com.siadmin.service.CutiService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cuti")
public class CutiController {

    private final CutiService cutiService;

    public CutiController(CutiService cutiService) {
        this.cutiService = cutiService;
    }

    @GetMapping
    public String cutiSaya(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        Karyawan karyawan = principal.getUser().getKaryawan();
        model.addAttribute("karyawan", karyawan);
        model.addAttribute("daftarCuti", karyawan != null ? cutiService.findByKaryawan(karyawan) : List.of());
        if (karyawan != null) {
            cutiService.tandaiSudahDibaca(karyawan);
        }
        return "cuti/list";
    }

    @GetMapping("/tambah")
    public String formTambah(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal.getUser().getKaryawan() == null) {
            return "redirect:/cuti";
        }
        model.addAttribute("cuti", new Cuti());
        model.addAttribute("jenisList", JenisCuti.values());
        return "cuti/form";
    }

    @PostMapping("/simpan")
    public String simpan(@AuthenticationPrincipal UserPrincipal principal,
                          @Valid @ModelAttribute("cuti") Cuti cuti,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        Karyawan karyawan = principal.getUser().getKaryawan();
        if (karyawan == null) {
            return "redirect:/cuti";
        }

        if (cuti.getTanggalMulai() != null && cuti.getTanggalSelesai() != null
                && !cutiService.isRentangValid(cuti.getTanggalMulai(), cuti.getTanggalSelesai())) {
            result.rejectValue("tanggalSelesai", "invalid",
                    "Tanggal selesai harus setelah atau sama dengan tanggal mulai");
        }

        if (result.hasErrors()) {
            model.addAttribute("jenisList", JenisCuti.values());
            return "cuti/form";
        }

        cutiService.ajukan(cuti, karyawan);
        redirectAttributes.addFlashAttribute("pesan", "Pengajuan cuti berhasil dikirim");
        return "redirect:/cuti";
    }

    @GetMapping("/kelola")
    public String kelola(Model model) {
        model.addAttribute("daftarCuti", cutiService.findAll());
        return "cuti/kelola";
    }

    @PostMapping("/setujui/{id}")
    public String setujui(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cutiService.setujui(id);
        redirectAttributes.addFlashAttribute("pesan", "Cuti berhasil disetujui");
        return "redirect:/cuti/kelola";
    }

    @PostMapping("/tolak/{id}")
    public String tolak(@PathVariable Long id, @RequestParam String catatanAdmin, RedirectAttributes redirectAttributes) {
        cutiService.tolak(id, catatanAdmin);
        redirectAttributes.addFlashAttribute("pesan", "Cuti berhasil ditolak");
        return "redirect:/cuti/kelola";
    }
}
