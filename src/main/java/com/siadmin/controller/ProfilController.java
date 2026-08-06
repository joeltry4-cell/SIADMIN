package com.siadmin.controller;

import com.siadmin.security.UserPrincipal;
import com.siadmin.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profil")
public class ProfilController {

    private final UserService userService;

    public ProfilController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/ganti-password")
    public String form() {
        return "profil/ganti-password";
    }

    @PostMapping("/ganti-password")
    public String simpan(@AuthenticationPrincipal UserPrincipal principal,
                          @RequestParam String passwordLama,
                          @RequestParam String passwordBaru,
                          @RequestParam String konfirmasiPassword,
                          Model model) {

        var user = principal.getUser();

        if (!userService.cekPassword(user, passwordLama)) {
            model.addAttribute("error", "Password lama salah");
            return "profil/ganti-password";
        }

        if (passwordBaru.length() < 8) {
            model.addAttribute("error", "Password baru minimal 8 karakter");
            return "profil/ganti-password";
        }

        if (!passwordBaru.equals(konfirmasiPassword)) {
            model.addAttribute("error", "Konfirmasi password tidak sama");
            return "profil/ganti-password";
        }

        userService.gantiPassword(user, passwordBaru);
        model.addAttribute("sukses", "Password berhasil diubah");
        return "profil/ganti-password";
    }
}
