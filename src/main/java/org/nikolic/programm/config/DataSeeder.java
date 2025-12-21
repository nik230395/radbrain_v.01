package org.nikolic.programm.config;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
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
            String adminEmail = "admin@radbrain.local";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User u = new User();
                u.setEmail(adminEmail);
                u.setPassword_hash(passwordEncoder.encode("adminpass"));
                u.setFullname("Initial Admin");
                u.setIs_active(true);
                u.setCreated_at(LocalDateTime.now());
                // assign ADMIN role
                u.setUserRole(UserRole.ADMIN);
                userRepository.save(u);
                System.out.println("Created admin user: " + adminEmail + " / password: adminpass");
            }
        };
    }
}