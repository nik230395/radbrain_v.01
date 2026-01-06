package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.PasswordResetCode;
import org.nikolic.programm.entities.User;
import org.springframework.data.jpa.repository. JpaRepository;
import org. springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa. repository.Query;
import org. springframework.data.repository.query. Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    // Find active code for user (not used)
    Optional<PasswordResetCode> findFirstByUserAndUsedOrderByCreatedAtDesc(User user, Boolean used);

    // Find latest unused code for user
    @Query("SELECT prc FROM PasswordResetCode prc WHERE prc.user = : user AND prc.used = false ORDER BY prc.createdAt DESC LIMIT 1")
    Optional<PasswordResetCode> findLatestUnusedCodeByUser(@Param("user") User user);

    // Find codes by user
    List<PasswordResetCode> findByUserOrderByCreatedAtDesc(User user);

    // Find valid (unexpired and unused) codes
    @Query("SELECT prc FROM PasswordResetCode prc WHERE prc.user = :user AND prc.used = false AND prc.createdAt > :validSince ORDER BY prc.createdAt DESC")
    List<PasswordResetCode> findValidCodes(@Param("user") User user, @Param("validSince") LocalDateTime validSince);

    // Clean up old used codes
    @Modifying
    @Query("DELETE FROM PasswordResetCode prc WHERE prc.used = true AND prc. createdAt < :cutoffDate")
    void deleteOldUsedCodes(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Clean up old unused codes (expired)
    @Modifying
    @Query("DELETE FROM PasswordResetCode prc WHERE prc. used = false AND prc.createdAt < :cutoffDate")
    void deleteOldUnusedCodes(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Mark all codes as used for user
    @Modifying
    @Query("UPDATE PasswordResetCode prc SET prc.used = true WHERE prc.user = :user AND prc.used = false")
    void markAllAsUsed(@Param("user") User user);

    // Count unused codes for user
    long countByUserAndUsed(User user, Boolean used);

    // Find codes created after date
    List<PasswordResetCode> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime since);
}