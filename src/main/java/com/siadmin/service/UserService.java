package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.User;
import com.siadmin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    private static final String PASSWORD_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Path TANDA_TANGAN_DIR = Paths.get("uploads", "tanda-tangan");
    private static final long MAKS_UKURAN_TANDA_TANGAN = 500_000;

    public List<User> findAll() {
        return userRepository.findAllWithKaryawan();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Akun dengan id " + id + " tidak ditemukan"));
    }

    public boolean usernameSudahAda(String username) {
        return userRepository.existsByUsername(username);
    }

    public User simpanAkunBaru(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.CREATE, "Akun", saved.getId(),
                "Akun " + saved.getUsername() + " (role " + saved.getRole() + ") dibuat");
        return saved;
    }

    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Akun dengan id " + id + " tidak ditemukan"));
        userRepository.deleteById(id);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.DELETE, "Akun", id,
                "Akun " + user.getUsername() + " dihapus");
    }

    public boolean cekPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void gantiPassword(User user, String passwordBaru) {
        user.setPassword(passwordEncoder.encode(passwordBaru));
        userRepository.save(user);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.UPDATE, "Akun", user.getId(),
                "Password akun " + user.getUsername() + " diubah");
    }

    public String resetPassword(Long userId) {
        User user = findById(userId);
        String passwordBaru = generatePasswordAcak();
        user.setPassword(passwordEncoder.encode(passwordBaru));
        userRepository.save(user);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.UPDATE, "Akun", user.getId(),
                "Password akun " + user.getUsername() + " direset oleh admin");
        return passwordBaru;
    }

    public void simpanTandaTangan(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong");
        }
        if (file.getSize() > MAKS_UKURAN_TANDA_TANGAN) {
            throw new IllegalArgumentException("Ukuran file maksimal 500 KB");
        }
        BufferedImage gambar = ImageIO.read(file.getInputStream());
        if (gambar == null) {
            throw new IllegalArgumentException("File harus berupa gambar (PNG/JPG) yang valid");
        }

        Files.createDirectories(TANDA_TANGAN_DIR);
        String namaFile = user.getId() + ".png";
        ImageIO.write(gambar, "png", TANDA_TANGAN_DIR.resolve(namaFile).toFile());

        user.setTandaTanganPath(namaFile);
        userRepository.save(user);
        auditLogService.log(AuditLogService.currentUsername(), AksiAudit.UPDATE, "Akun", user.getId(),
                "Tanda tangan akun " + user.getUsername() + " diperbarui");
    }

    public Optional<byte[]> bacaTandaTangan(User user) {
        if (user.getTandaTanganPath() == null) {
            return Optional.empty();
        }
        Path path = TANDA_TANGAN_DIR.resolve(user.getTandaTanganPath());
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String generatePasswordAcak() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(PASSWORD_CHARSET.charAt(RANDOM.nextInt(PASSWORD_CHARSET.length())));
        }
        return sb.toString();
    }
}
