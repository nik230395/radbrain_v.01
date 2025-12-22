package org.nikolic.programm. config;

import org.nikolic. programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm. repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin already exists
            if (userRepository. findByEmail("admin@radbrain.com").isPresent()) {
                System.out.println("Admin user already exists");
                return;
            }

            // ✅ Fixed: Use proper enum values
            User admin = new User();
            admin.setEmail("admin@radbrain.com");
            admin.setPassword_hash(passwordEncoder.encode("admin123"));
            admin.setFullname("Admin User");
            admin. setIs_active(true);
            admin.setRole(UserRole.ADMIN); // ✅ Use enum directly
            admin.setCreated_at(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("Created admin user: admin@radbrain.com (password: admin123)");

            // Create test user
            if (userRepository.findByEmail("user@radbrain.com").isEmpty()) {
                User testUser = new User();
                testUser.setEmail("user@radbrain.com");
                testUser.setPassword_hash(passwordEncoder.encode("user123"));
                testUser.setFullname("Test User");
                testUser. setIs_active(true);
                testUser.setRole(UserRole.USER); // ✅ Use enum directly
                testUser.setCreated_at(LocalDateTime.now());

                userRepository.save(testUser);
                System.out.println("Created test user: user@radbrain. com (password: user123)");
            }
        };
    }
}