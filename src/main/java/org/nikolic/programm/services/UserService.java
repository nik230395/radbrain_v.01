package org.nikolic.programm.services;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic.programm.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ✅ FIXED UserService
 *
 * Changes:
 * - Uses isActive() instead of getIs_active()
 * - Added proper null checks
 * - Improved error handling
 * - Added comprehensive logging
 * - Simplified authentication methods
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Load user by username (email) for Spring Security
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // ✅ FIXED: Use isActive() instead of getIs_active()
        if (!user.isActive()) {
            logger.warn("Attempted login by inactive user: {}", email);
            throw new UsernameNotFoundException("User account is inactive");
        }

        Collection<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    /**
     * ✅ Die zentrale Save-Methode
     * Verarbeitet sowohl neue User als auch Updates.
     */
    public User save(User user) {
        logger.info("Saving user: {}", user.getEmail());

        // Falls es ein neuer User ist und das Passwort noch im Klartext vorliegt
        if (user.getId() == null && user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    /**
     * Get authenticated user from Authentication object
     */
    @Transactional(readOnly = true)
    public Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            logger.debug("Authentication is null");
            return Optional.empty();
        }

        String email = authentication.getName();
        if (email == null || email.trim().isEmpty()) {
            logger.debug("Email from authentication is null or empty");
            return Optional.empty();
        }

        return findByEmail(email);
    }

    /**
     * Check if user is admin
     * ✅ FIXED: Added null check for authentication
     */
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Authentication authentication, UserRole role) {
        if (authentication == null || role == null) {
            return false;
        }

        String roleAuthority = "ROLE_" + role.name();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleAuthority));
    }

    /**
     * Create new user
     */
    public User createUser(String email, String fullname, String password, UserRole role) {
        logger.info("Creating new user with email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        String cleanEmail = email.toLowerCase().trim();

        // Check if user already exists
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("User with email " + cleanEmail + " already exists");
        }

        User user = new User();
        user.setEmail(cleanEmail);
        user.setFullname(fullname != null ? fullname.trim() : "");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role != null ? role : UserRole.USER);
        user.setActive(true);  // ✅ FIXED: Use setActive() method
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        logger.info("Created user with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Update user
     */
    public User updateUser(Long id, String fullname, UserRole role) {
        logger.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

        if (fullname != null && !fullname.trim().isEmpty()) {
            user.setFullname(fullname.trim());
        }

        if (role != null) {
            user.setRole(role);
        }

        User updated = userRepository.save(user);
        logger.info("Updated user: {}", id);

        return updated;
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Reset password (for forgot password flow)
     */
    public void resetPassword(Long userId, String newPassword) {
        logger.info("Resetting password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password reset successfully for user: {}", userId);
    }

    /**
     * Activate user
     * ✅ FIXED: Use activate() method
     */
    public void activateUser(Long userId) {
        logger.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        user.activate();  // ✅ FIXED: Use helper method
        userRepository.save(user);

        logger.info("User activated: {}", userId);
    }

    /**
     * Deactivate user
     * ✅ FIXED: Use deactivate() method
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        user.deactivate();  // ✅ FIXED: Use helper method
        userRepository.save(user);

        logger.info("User deactivated: {}", userId);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        logger.info("Deleting user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found with id: " + userId);
        }

        userRepository.deleteById(userId);
        logger.info("User deleted: {}", userId);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all active users
     * ✅ FIXED: Uses repository method with camelCase
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findActiveUsers();
    }

    /**
     * Get all inactive users
     */
    @Transactional(readOnly = true)
    public List<User> getInactiveUsers() {
        return userRepository.findInactiveUsers();
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get all admin users
     */
    @Transactional(readOnly = true)
    public List<User> getAdminUsers() {
        return userRepository.findAdmins();
    }

    /**
     * Search users by name or email
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchByEmailOrName(searchTerm.trim());
    }

    /**
     * Get user statistics
     * ✅ FIXED: Uses repository methods with camelCase
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByIsActive(true));
        stats.put("inactiveUsers", userRepository.countByIsActive(false));
        stats.put("adminUsers", userRepository.countByRole(UserRole.ADMIN));
        stats.put("regularUsers", userRepository.countByRole(UserRole.USER));

        // Users created in last 7 days
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        stats.put("newUsersThisWeek", userRepository.countUsersCreatedAfter(weekAgo));

        // Users created in last 30 days
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        stats.put("newUsersThisMonth", userRepository.countUsersCreatedAfter(monthAgo));

        return stats;
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    /**
     * Promote user to admin
     */
    public void promoteToAdmin(Long userId) {
        logger.info("Promoting user to admin: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        logger.info("User promoted to admin: {}", userId);
    }

    /**
     * Demote admin to regular user
     */
    public void demoteToUser(Long userId) {
        logger.info("Demoting admin to user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        // Ensure at least one admin remains
        if (user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last admin user");
            }
        }

        user.setRole(UserRole.USER);
        userRepository.save(user);

        logger.info("Admin demoted to user: {}", userId);
    }

    /**
     * Get user count
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Get active user count
     * ✅ FIXED: Uses repository method with camelCase
     */
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countByIsActive(true);
    }
}