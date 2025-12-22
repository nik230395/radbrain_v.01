package org.nikolic.programm.services;

import org.nikolic.  programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.  nikolic.programm.repositories.  UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.  userdetails.UserDetailsService;
import org.springframework.security.  core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.  util.  List;
import java.util.  Optional;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.  getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found:   " + username));

        return org.springframework.security.core. userdetails.User.  builder()
                .username(user. getEmail())
                .password(user.getPassword_hash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())))
                .accountExpired(false)
                .accountLocked(!  user.isActive())
                .credentialsExpired(false)
                .disabled(!  user.isEmailVerified()) // Verwendet @Transient Feld
                .build();
    }

    // Authentication methods
    public Optional<User> authenticateUser(String email, String password) {
        if (email == null || password == null) {
            return Optional.  empty();
        }

        String cleanEmail = email.toLowerCase().trim();

        try {
            Optional<User> userOpt = userRepository.findByEmail(cleanEmail);

            if (userOpt.isEmpty()) {
                return Optional. empty();
            }

            User user = userOpt.get();

            if (!user.isActive()) {
                return Optional.empty();
            }

            // Für jetzt: alle Users sind "email verified"
            if (!user.isEmailVerified()) {
                return Optional.empty();
            }

            if (!  passwordEncoder.matches(password, user.getPassword_hash())) {
                return Optional.empty();
            }

            return Optional.  of(user);

        } catch (Exception e) {
            logger.error("Authentication error for user: {}", cleanEmail, e);
            return Optional.empty();
        }
    }

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

            User user = userOpt.  get();

            if (! user.isActive()) {
                return AuthenticationResult.failed("Konto ist deaktiviert");
            }

            // Für jetzt: alle aktive Users sind "email verified"
            if (! user.isEmailVerified()) {
                return AuthenticationResult. needsVerification("E-Mail-Adresse muss bestätigt werden");
            }

            if (! passwordEncoder.  matches(password, user.getPassword_hash())) {
                return AuthenticationResult.failed("Ungültige Anmeldedaten");
            }

            return AuthenticationResult.success(user);

        } catch (Exception e) {
            logger.error("Authentication error for user: {}", cleanEmail, e);
            return AuthenticationResult.failed("Anmeldung fehlgeschlagen");
        }
    }

    // Standard methods
    public Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return Optional.empty();

        String email = authentication.getName();
        if (email == null) return Optional.empty();

        return findByEmail(email);
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean hasRole(Authentication authentication, String roleName) {
        if (authentication == null || roleName == null) return false;

        String roleAuthority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authentication. getAuthorities().stream()
                .anyMatch(auth -> auth. getAuthority().equals(roleAuthority));
    }

    // Basic CRUD
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.  toLowerCase().trim());
    }

    public Optional<User> findById(Long id) {
        return userRepository.  findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User createUser(String email, String fullname, String passwordHash, UserRole role) {
        User user = new User();
        user.setEmail(email.  toLowerCase().trim());
        user.setFullname(fullname.  trim());
        user.setPassword_hash(passwordHash);
        user.setRole(role != null ? role : UserRole.USER);
        user.setIs_active(true); // ✅ Direkt aktiv für Simplicity
        user.setEmailVerified(true); // ✅ @Transient - direkt verified
        user.setCreated_at(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Simple checks
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email. toLowerCase().trim());
    }

    public boolean hasAdminUser() {
        return userRepository.  hasActiveAdmin();
    }

    public Optional<User> getFirstAdmin() {
        return userRepository.findFirstAdmin();
    }

    public User createSystemAdminIfNeeded(String email, String fullname, String password) {
        if (hasAdminUser()) {
            throw new RuntimeException("Admin bereits vorhanden");
        }

        if (emailExists(email)) {
            throw new RuntimeException("E-Mail bereits registriert");
        }

        return createUser(email, fullname, passwordEncoder.encode(password), UserRole.ADMIN);
    }

    // ✅ Vereinfachte Statistics
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

        return new UserStatistics(total, active, admins, 0); // unverified = 0
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByEmailContainingIgnoreCaseOrFullnameContainingIgnoreCase(searchTerm, searchTerm);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findActiveUsers();
    }

    public List<User> getAllAdmins() {
        return userRepository.  findByRole(UserRole. ADMIN);
    }

    // DTOs (unchanged)
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final boolean needsVerification;

        private AuthenticationResult(boolean success, String message, User user, boolean needsVerification) {
            this.  success = success;
            this.  message = message;
            this.  user = user;
            this.  needsVerification = needsVerification;
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
            this.  unverifiedUsers = unverifiedUsers;
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