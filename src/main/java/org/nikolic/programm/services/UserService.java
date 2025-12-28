package org.nikolic.programm.services;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserService - Angepasst an neue User Entity
 *
 * Änderungen:
 * - Alle Methoden verwenden neue User-API
 * - setPassword_hash() → setPasswordHash()
 * - setIs_active() → setActive()
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // ✅ ANGEPASST: Verwende neue Methoden
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash()) // ✅ NEU
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())))
                .accountExpired(false)
                .accountLocked(!user.isActive()) // ✅ NEU
                .credentialsExpired(false)
                .disabled(!user.isEmailVerified()) // ✅ NEU
                .build();
    }

    /**
     * Einfache Authentifizierung
     */
    public Optional<User> authenticateUser(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        String cleanEmail = email.toLowerCase().trim();

        try {
            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);

            if (userOpt.isEmpty()) {
                return Optional.empty();
            }

            User user = userOpt.get();

            // ✅ ANGEPASST: Verwende neue Methoden
            if (!user.isActive()) {
                return Optional.empty();
            }

            if (!user.isEmailVerified()) {
                return Optional.empty();
            }

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.empty();
            }

            return Optional.of(user);

        } catch (Exception e) {
            logger.error("Authentication error for user: {}", cleanEmail, e);
            return Optional.empty();
        }
    }

    /**
     * Authentifizierung mit detailliertem Result
     */
    public AuthenticationResult authenticateUserDetailed(String email, String password) {
        if (email == null || password == null) {
            return AuthenticationResult.failed("E-Mail und Passwort sind erforderlich");
        }

        String cleanEmail = email.toLowerCase().trim();

        try {
            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);

            if (userOpt.isEmpty()) {
                return AuthenticationResult.failed("Ungültige Anmeldedaten");
            }

            User user = userOpt.get();

            // ✅ ANGEPASST: Verwende neue Methoden
            if (!user.isActive()) {
                return AuthenticationResult.failed("Konto ist deaktiviert");
            }

            if (!user.isEmailVerified()) {
                return AuthenticationResult.needsVerification("E-Mail-Adresse muss bestätigt werden");
            }

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                return AuthenticationResult.failed("Ungültige Anmeldedaten");
            }

            return AuthenticationResult.success(user);

        } catch (Exception e) {
            logger.error("Authentication error for user: {}", cleanEmail, e);
            return AuthenticationResult.failed("Anmeldung fehlgeschlagen");
        }
    }

    /**
     * Hole authentifizierten User
     */
    public Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return Optional.empty();

        String email = authentication.getName();
        if (email == null) return Optional.empty();

        return findByEmail(email);
    }

    /**
     * Prüfe ob User Admin ist
     */
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Prüfe ob User Rolle hat
     */
    public boolean hasRole(Authentication authentication, String roleName) {
        if (authentication == null || roleName == null) return false;

        String roleAuthority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleAuthority));
    }

    // Basic CRUD

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Erstelle neuen User
     */
    public User createUser(String email, String fullname, String passwordHash, UserRole role) {
        User user = new User();
        user.setEmail(email.toLowerCase().trim());
        user.setFullname(fullname.trim());
        user.setPasswordHash(passwordHash); // ✅ ANGEPASST
        user.setRole(role != null ? role : UserRole.USER);
        user.setActive(true); // ✅ ANGEPASST
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Prüfe ob Email existiert
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    /**
     * Prüfe ob Admin User existiert
     */
    public boolean hasAdminUser() {
        return userRepository.hasActiveAdmin();
    }

    /**
     * Hole ersten Admin
     */
    public Optional<User> getFirstAdmin() {
        return userRepository.findFirstAdmin();
    }

    /**
     * Erstelle System Admin falls nötig
     */
    public User createSystemAdminIfNeeded(String email, String fullname, String password) {
        if (hasAdminUser()) {
            throw new RuntimeException("Admin bereits vorhanden");
        }

        if (emailExists(email)) {
            throw new RuntimeException("E-Mail bereits registriert");
        }

        return createUser(email, fullname, passwordEncoder.encode(password), UserRole.ADMIN);
    }

    // Statistics

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    public UserStatistics getUserStatistics() {
        long total = userRepository.count();
        long active = userRepository.countActiveUsers();
        long admins = userRepository.countByRole(UserRole.ADMIN);

        return new UserStatistics(total, active, admins, 0);
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByEmailContainingIgnoreCaseOrFullnameContainingIgnoreCase(
                searchTerm, searchTerm
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findActiveUsers();
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRole(UserRole.ADMIN);
    }

    // DTOs

    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final boolean needsVerification;

        private AuthenticationResult(boolean success, String message, User user, boolean needsVerification) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.needsVerification = needsVerification;
        }

        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(true, "Authentication successful", user, false);
        }

        public static AuthenticationResult failed(String message) {
            return new AuthenticationResult(false, message, null, false);
        }

        public static AuthenticationResult needsVerification(String message) {
            return new AuthenticationResult(false, message, null, true);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public boolean needsVerification() { return needsVerification; }
    }

    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long adminUsers;
        private final long unverifiedUsers;

        public UserStatistics(long totalUsers, long activeUsers, long adminUsers, long unverifiedUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
            this.unverifiedUsers = unverifiedUsers;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getUnverifiedUsers() { return unverifiedUsers; }
        public long getInactiveUsers() { return totalUsers - activeUsers; }

        public double getActiveUserPercentage() {
            return totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0;
        }
    }
}