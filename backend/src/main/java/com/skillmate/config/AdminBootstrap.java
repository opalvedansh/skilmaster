package com.skillmate.config;

import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import com.skillmate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first ADMIN from environment variables (no credentials live in source code). */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UserRepository users; private final PasswordEncoder encoder;
    @Value("${skillmate.admin.email:}") private String email;
    @Value("${skillmate.admin.password:}") private String password;
    @Value("${skillmate.admin.name:Administrator}") private String name;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        if (users.existsByEmailIgnoreCase(email.trim())) return;
        User admin = new User();
        admin.setName(name); admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(encoder.encode(password)); admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Bootstrap admin account created for {}", admin.getEmail());
    }
}
