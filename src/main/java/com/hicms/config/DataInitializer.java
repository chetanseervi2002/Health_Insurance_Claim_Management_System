package com.hicms.config;

import com.hicms.entity.Role;
import com.hicms.entity.User;
import com.hicms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer to create default users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Create default admin user
        createUserIfNotExists("admin", "admin123", "admin@hicms.com", "System Administrator", Role.ADMIN);
        
        // Create default agent user
        createUserIfNotExists("agent", "agent123", "agent@hicms.com", "Insurance Agent", Role.AGENT);
        
        // Create default claim adjuster user
        createUserIfNotExists("adjuster", "adjuster123", "adjuster@hicms.com", "Claim Adjuster", Role.CLAIM_ADJUSTER);
        
        // Create default customer user
        createUserIfNotExists("user", "user123", "user@hicms.com", "John Customer", Role.USER);
        
        log.info("Default users initialized successfully");
    }
    
    private void createUserIfNotExists(String username, String password, String email, String fullName, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .fullName(fullName)
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Created default {} user: {}", role, username);
        }
    }
}