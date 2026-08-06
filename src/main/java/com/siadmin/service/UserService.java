package com.siadmin.service;

import com.siadmin.model.User;
import com.siadmin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAllWithKaryawan();
    }

    public boolean usernameSudahAda(String username) {
        return userRepository.existsByUsername(username);
    }

    public User simpanAkunBaru(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean cekPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void gantiPassword(User user, String passwordBaru) {
        user.setPassword(passwordEncoder.encode(passwordBaru));
        userRepository.save(user);
    }
}
