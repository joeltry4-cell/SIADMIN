package com.siadmin.service;

import com.siadmin.model.AksiAudit;
import com.siadmin.model.User;
import com.siadmin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

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

    private String generatePasswordAcak() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(PASSWORD_CHARSET.charAt(RANDOM.nextInt(PASSWORD_CHARSET.length())));
        }
        return sb.toString();
    }
}
