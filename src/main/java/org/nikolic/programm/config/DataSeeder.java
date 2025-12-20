package org.nikolic.programm.config;

import org.nikolic.programm.entities.Role;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.RoleRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<Role> maybeAdmin = roleRepository.findByName("ROLE_ADMIN");
            Role adminRole = maybeAdmin.orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN", "Administrator")));

            String adminEmail = "admin@radbrain.local";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User u = new User();
                u.setEmail(adminEmail);
                u.setPassword_hash(passwordEncoder.encode("adminpass"));
                u.setFullname("Initial Admin");
                u.setIs_active(true);
                u.setCreated_at(LocalDateTime.now());
                // add existing managed role (no cascade)
                u.getRoles().add(adminRole);
                userRepository.save(u);
                System.out.println("Created admin user: " + adminEmail + " / password: adminpass");
            }
        };
    }
}