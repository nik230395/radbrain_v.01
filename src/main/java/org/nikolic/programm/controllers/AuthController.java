package org.nikolic.programm.controllers;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password required"));
        }

        Optional<User> uo = userRepository.findByEmail(email);
        if (uo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        User user = uo.get();

        if (!Boolean.TRUE.equals(user.getIs_active())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Account not verified"));
        }

        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.createToken(user.getEmail(), user.getId());
        
        // Include roles in response
        java.util.List<String> roleNames = new java.util.ArrayList<>();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> roleNames.add(role.getName()));
        } else if (user.getRole() != null) {
            roleNames.add(user.getRole());
        }
        
        return ResponseEntity.ok(Map.of(
                "message", "login_success",
                "id", user.getId(),
                "email", user.getEmail(),
                "fullname", user.getFullname(),
                "token", token,
                "roles", roleNames
        ));
    }
}