package org.nikolic.programm.config;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * DataSeeder - erstellt Test-Daten beim Start
 * ✅ FIX: Nutzt jetzt UserRole ENUM
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Prüfe ob Admin bereits existiert
            if (userRepository.findByEmail("admin@radbrain.com").isPresent()) {
                System.out.println("✅ Admin user already exists");
                return;
            }

            // ✅ FIX: Erstelle Admin mit ENUM (nicht Role Entity)
            User admin = new User();
            admin.setEmail("admin@radbrain.com");
            admin.setPassword_hash(passwordEncoder.encode("admin123"));
            admin.setFullname("Admin User");
            admin.setIs_active(true);
            admin.setRole(UserRole.ADMIN); // ✅ ENUM statt Role Entity

            userRepository.save(admin);
            System.out.println("✅ Created admin user: admin@radbrain.com (password: admin123)");

            // Optional: Erstelle Test-User
            if (userRepository.findByEmail("user@radbrain.com").isEmpty()) {
                User testUser = new User();
                testUser.setEmail("user@radbrain.com");
                testUser.setPassword_hash(passwordEncoder.encode("user123"));
                testUser.setFullname("Test User");
                testUser.setIs_active(true);
                testUser.setRole(UserRole.USER); // ✅ ENUM

                userRepository.save(testUser);
                System.out.println("✅ Created test user: user@radbrain.com (password: user123)");
            }
        };
    }
}