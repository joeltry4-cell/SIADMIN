package com.siadmin.controller;

import com.siadmin.model.Karyawan;
import com.siadmin.model.Lembur;
import com.siadmin.security.UserPrincipal;
import com.siadmin.service.LemburService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/lembur")
public class LemburController {

    private final LemburService lemburService;

    public LemburController(LemburService lemburService) {
        this.lemburService = lemburService;
    }

    @GetMapping
    public String lemburSaya(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        Karyawan karyawan = principal.getUser().getKaryawan();
        model.addAttribute("karyawan", karyawan);
        model.addAttribute("daftarLembur", karyawan != null ? lemburService.findByKaryawan(karyawan) : List.of());
        return "lembur/list";
    }

    @GetMapping("/tambah")
    public String formTambah(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal.getUser().getKaryawan() == null) {
            return "redirect:/lembur";
        }
        Lembur lembur = new Lembur();
        lembur.setTanggal(LocalDate.now());
        model.addAttribute("lembur", lembur);
        return "lembur/form";
    }

    @PostMapping("/simpan")
    public String simpan(@AuthenticationPrincipal UserPrincipal principal,
                          @Valid @ModelAttribute("lembur") Lembur lembur,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {

        Karyawan karyawan = principal.getUser().getKaryawan();
        if (karyawan == null) {
            return "redirect:/lembur";
        }

        if (lembur.getJamMulai() != null && lembur.getJamSelesai() != null
                && !lemburService.isJamValid(lembur.getJamMulai(), lembur.getJamSelesai())) {
            result.rejectValue("jamSelesai", "invalid", "Jam selesai harus setelah jam mulai");
        }

        if (result.hasErrors()) {
            return "lembur/form";
        }

        lemburService.ajukan(lembur, karyawan);
        redirectAttributes.addFlashAttribute("pesan", "Pengajuan lembur berhasil dikirim");
        return "redirect:/lembur";
    }

    @GetMapping("/kelola")
    public String kelola(Model model) {
        model.addAttribute("daftarLembur", lemburService.findAll());
        return "lembur/kelola";
    }

    @PostMapping("/setujui/{id}")
    public String setujui(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        lemburService.setujui(id);
        redirectAttributes.addFlashAttribute("pesan", "Lembur berhasil disetujui");
        return "redirect:/lembur/kelola";
    }

    @PostMapping("/tolak/{id}")
    public String tolak(@PathVariable Long id, @RequestParam String catatanAdmin, RedirectAttributes redirectAttributes) {
        lemburService.tolak(id, catatanAdmin);
        redirectAttributes.addFlashAttribute("pesan", "Lembur berhasil ditolak");
        return "redirect:/lembur/kelola";
    }
}
