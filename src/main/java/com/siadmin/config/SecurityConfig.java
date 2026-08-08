package com.siadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                .requestMatchers("/karyawan/**").hasRole("ADMIN")
                .requestMatchers("/akun/**").hasRole("ADMIN")
                .requestMatchers("/absensi/rekap", "/absensi/hapus/**").hasRole("ADMIN")
                .requestMatchers("/cuti/kelola", "/cuti/setujui/**", "/cuti/tolak/**").hasRole("ADMIN")
                .requestMatchers("/lembur/kelola", "/lembur/setujui/**", "/lembur/tolak/**").hasRole("ADMIN")
                .requestMatchers("/laporan/**").hasRole("ADMIN")
                .requestMatchers("/audit-log/**").hasRole("ADMIN")
                .requestMatchers("/dokumen/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(LogoutConfigurer::permitAll);

        return http.build();
    }
}
