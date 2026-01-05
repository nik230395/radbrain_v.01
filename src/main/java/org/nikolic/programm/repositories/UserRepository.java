package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email (case-insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists (case-insensitive)
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find all active users
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Convenience method for finding active users
     */
    default List<User> findActiveUsers() {
        return findByIsActive(true);
    }

    /**
     * Convenience method for finding inactive users
     */
    default List<User> findInactiveUsers() {
        return findByIsActive(false);
    }

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find users by role and active status
     */
    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);

    /**
     * Find all admin users
     */
    default List<User> findAdmins() {
        return findByRole(UserRole.ADMIN);
    }

    /**
     * Find active admin users
     */
    default List<User> findActiveAdmins() {
        return findByRoleAndIsActive(UserRole.ADMIN, true);
    }

    /**
     * Check if any active admin exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    boolean hasActiveAdmin();

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Count active users
     */
    long countByIsActive(Boolean isActive);

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByEmailOrName(@Param("searchTerm") String searchTerm);

    /**
     * Find users created after a specific date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);

    /**
     * Find users with unverified email
     */
    @Query("SELECT u FROM User u WHERE u.emailVerification IS NOT NULL " +
            "AND u.emailVerification.verified = false " +
            "AND u.isActive = false")
    List<User> findUnverifiedUsers();

    /**
     * Find users by fullname containing (case-insensitive)
     */
    List<User> findByFullnameContainingIgnoreCase(String name);

    /**
     * Get user statistics
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdminUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);

    long countByCreatedAtAfter(LocalDateTime date);
    long countByRoleContaining(String role);
}