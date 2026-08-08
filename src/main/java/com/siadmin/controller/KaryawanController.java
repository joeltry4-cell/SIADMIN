package com.siadmin.controller;

import com.siadmin.model.JenisKelamin;
import com.siadmin.model.Karyawan;
import com.siadmin.model.StatusKepegawaian;
import com.siadmin.service.KaryawanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/karyawan")
public class KaryawanController {

    private final KaryawanService karyawanService;

    public KaryawanController(KaryawanService karyawanService) {
        this.karyawanService = karyawanService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("daftarKaryawan", karyawanService.search(q));
        model.addAttribute("keyword", q);
        return "karyawan/list";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("karyawan", new Karyawan());
        model.addAttribute("jenisKelaminList", JenisKelamin.values());
        model.addAttribute("statusList", StatusKepegawaian.values());
        return "karyawan/form";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        model.addAttribute("karyawan", karyawanService.findById(id));
        model.addAttribute("jenisKelaminList", JenisKelamin.values());
        model.addAttribute("statusList", StatusKepegawaian.values());
        return "karyawan/form";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Karyawan karyawan = karyawanService.findById(id);
        model.addAttribute("karyawan", karyawan);
        model.addAttribute("jadwalCutiBerikutnya", karyawanService.jadwalCutiBerikutnya(karyawan).orElse(null));
        return "karyawan/detail";
    }

    @PostMapping("/simpan")
    public String simpan(@Valid @ModelAttribute("karyawan") Karyawan karyawan,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (karyawanService.isNipDuplicate(karyawan.getNip(), karyawan.getId())) {
            result.rejectValue("nip", "duplicate", "NIP sudah digunakan karyawan lain");
        }

        if (result.hasErrors()) {
            model.addAttribute("jenisKelaminList", JenisKelamin.values());
            model.addAttribute("statusList", StatusKepegawaian.values());
            return "karyawan/form";
        }

        karyawanService.save(karyawan);
        redirectAttributes.addFlashAttribute("pesan", "Data karyawan berhasil disimpan");
        return "redirect:/karyawan";
    }

    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        karyawanService.deleteById(id);
        redirectAttributes.addFlashAttribute("pesan", "Data karyawan berhasil dihapus");
        return "redirect:/karyawan";
    }
}
