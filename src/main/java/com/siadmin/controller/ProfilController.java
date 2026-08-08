package com.siadmin.controller;

import com.siadmin.security.UserPrincipal;
import com.siadmin.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @GetMapping("/tanda-tangan")
    public String formTandaTangan(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("adaTandaTangan", principal.getUser().getTandaTanganPath() != null);
        return "profil/tanda-tangan";
    }

    @PostMapping("/tanda-tangan")
    public String simpanTandaTangan(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam("file") MultipartFile file,
                                     Model model) {
        try {
            userService.simpanTandaTangan(principal.getUser(), file);
            model.addAttribute("sukses", "Tanda tangan berhasil disimpan");
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("adaTandaTangan", principal.getUser().getTandaTanganPath() != null);
        return "profil/tanda-tangan";
    }

    @GetMapping("/tanda-tangan/gambar")
    public void gambarTandaTangan(@AuthenticationPrincipal UserPrincipal principal, HttpServletResponse response) throws IOException {
        byte[] gambar = userService.bacaTandaTangan(principal.getUser()).orElse(null);
        if (gambar == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("image/png");
        response.getOutputStream().write(gambar);
    }
}
