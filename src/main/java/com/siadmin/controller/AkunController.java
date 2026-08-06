package com.siadmin.controller;

import com.siadmin.model.Role;
import com.siadmin.model.User;
import com.siadmin.service.KaryawanService;
import com.siadmin.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/akun")
public class AkunController {

    private final UserService userService;
    private final KaryawanService karyawanService;

    public AkunController(UserService userService, KaryawanService karyawanService) {
        this.userService = userService;
        this.karyawanService = karyawanService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("daftarAkun", userService.findAll());
        return "akun/list";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("roleList", Role.values());
        model.addAttribute("daftarKaryawan", karyawanService.findAll());
        return "akun/form";
    }

    @PostMapping("/simpan")
    public String simpan(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam Role role,
                          @RequestParam(required = false) Long karyawanId,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (userService.usernameSudahAda(username)) {
            model.addAttribute("error", "Username sudah digunakan");
            model.addAttribute("roleList", Role.values());
            model.addAttribute("daftarKaryawan", karyawanService.findAll());
            return "akun/form";
        }

        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);
        if (karyawanId != null) {
            user.setKaryawan(karyawanService.findById(karyawanId));
        }

        userService.simpanAkunBaru(user, password);
        redirectAttributes.addFlashAttribute("pesan", "Akun berhasil dibuat");
        return "redirect:/akun";
    }

    @PostMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteById(id);
        redirectAttributes.addFlashAttribute("pesan", "Akun berhasil dihapus");
        return "redirect:/akun";
    }
}
