package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.RegisterRequest;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.security.JwtUtil;
import org.nikolic.programm.services.RegistrationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserApiController {

    private final UserRepository userRepository;
    private final RegistrationCacheService registrationCacheService;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private RoleRepository roleRepository;

    public UserApiController(UserRepository userRepository,
                             RegistrationCacheService registrationCacheService,
                             JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.registrationCacheService = registrationCacheService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            registrationCacheService.createRegistrationAndSendCode(
                    request.getFullname(), request.getEmail(), request.getPassword()
            );
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", iae.getMessage()));
        } catch (MailException mex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send verification email. Bitte pruefe Email-Konfiguration."));
        }
        return ResponseEntity.ok(Map.of("message", "verification_sent"));
    }

    /**
     * Verify endpoint: prüft Code, erzeugt den User (bei Cache‑Flow) und liefert beim Erfolg gleich ein JWT zurück.
     * Response (on success):
     * {
     *   "message":"verified",
     *   "id": 123,
     *   "email":"x@y.de",
     *   "fullname":"Max Mustermann",
     *   "token": "eyJ..."
     * }
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and code required"));
        }

        // registrationCacheService.verifyAndCreateUser returns the created User (or null if invalid/expired)
        User created = registrationCacheService.verifyAndCreateUser(email, code);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid or expired code"));
        }

        // create JWT token for the newly created user
        String token = jwtUtil.createToken(created.getEmail(), created.getId());

        // Include roles in response
        java.util.List<String> roleNames = new java.util.ArrayList<>();
        if (created.getRoles() != null && !created.getRoles().isEmpty()) {
            created.getRoles().forEach(role -> roleNames.add(role.getName()));
        } else if (created.getRole() != null) {
            roleNames.add(created.getRole());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "verified");
        resp.put("id", created.getId());
        resp.put("email", created.getEmail());
        resp.put("fullname", created.getFullname());
        resp.put("token", token);
        resp.put("roles", roleNames);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) return ResponseEntity.badRequest().body(Map.of("error", "email required"));
        try {
            registrationCacheService.resendCode(email);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "no pending registration"));
        } catch (MailException mex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send verification email"));
        }
        return ResponseEntity.ok(Map.of("message", "verification_sent"));
    }
}