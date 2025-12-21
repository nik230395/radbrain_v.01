package org.nikolic.programm.config;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.enums.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create admin user if not exists
            String adminEmail = "admin@radbrain.local";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword_hash(passwordEncoder.encode("adminpass"));
                admin.setFullname("Initial Admin");
                admin.setIs_active(true);
                admin.setCreated_at(LocalDateTime.now());
                admin.setUserRole(UserRole.ADMIN);
                userRepository.save(admin);
                System.out.println("Created admin user: " + adminEmail + " / password: adminpass");
            }

            // Create test user if not exists
            String testEmail = "test@radbrain.local";
            if (userRepository.findByEmail(testEmail).isEmpty()) {
                User testUser = new User();
                testUser.setEmail(testEmail);
                testUser.setPassword_hash(passwordEncoder.encode("testpass"));
                testUser.setFullname("Test User");
                testUser.setIs_active(true);
                testUser.setCreated_at(LocalDateTime.now());
                testUser.setUserRole(UserRole.USER);
                userRepository.save(testUser);
                System.out.println("Created test user: " + testEmail + " / password: testpass");
            }
        };
    }
}