package org.nikolic.programm.config;

import org.nikolic.programm.entities.EmailVerification;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * DataSeeder - Angepasst an neue User Entity
 *
 * HINWEIS: Diese Klasse ist optional, da DataInitializer bereits existiert!
 * Falls du beide hast, solltest du eine davon löschen.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin already exists
            if (userRepository.findByEmail("admin@radbrain.com").isPresent()) {
                System.out.println("Admin user already exists");
                return;
            }

            // ✅ ANGEPASST: Verwende neue Methodennamen
            User admin = new User();
            admin.setEmail("admin@radbrain.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123")); // ✅ NEU
            admin.setFullname("Admin User");
            admin.setActive(true); // ✅ NEU
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());

            // ✅ NEU: EmailVerification Entity (bereits verifiziert)
            EmailVerification verification = new EmailVerification();
            verification.setUser(admin);
            verification.setVerificationCode("000000");
            verification.setExpiresAt(LocalDateTime.now().plusYears(10));
            verification.setVerified(true);
            verification.setVerifiedAt(LocalDateTime.now());
            verification.setCreatedAt(LocalDateTime.now());

            admin.setEmailVerification(verification);

            userRepository.save(admin);
            System.out.println("Created admin user: admin@radbrain.com (password: admin123)");

            // Create test user
            if (userRepository.findByEmail("user@radbrain.com").isEmpty()) {
                User testUser = new User();
                testUser.setEmail("user@radbrain.com");
                testUser.setPasswordHash(passwordEncoder.encode("user123")); // ✅ NEU
                testUser.setFullname("Test User");
                testUser.setActive(true); // ✅ NEU
                testUser.setRole(UserRole.USER);
                testUser.setCreatedAt(LocalDateTime.now());

                // ✅ NEU: EmailVerification Entity (bereits verifiziert)
                EmailVerification testVerification = new EmailVerification();
                testVerification.setUser(testUser);
                testVerification.setVerificationCode("000000");
                testVerification.setExpiresAt(LocalDateTime.now().plusYears(10));
                testVerification.setVerified(true);
                testVerification.setVerifiedAt(LocalDateTime.now());
                testVerification.setCreatedAt(LocalDateTime.now());

                testUser.setEmailVerification(testVerification);

                userRepository.save(testUser);
                System.out.println("Created test user: user@radbrain.com (password: user123)");
            }
        };
    }
}