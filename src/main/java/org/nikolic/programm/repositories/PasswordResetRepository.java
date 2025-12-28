package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.PasswordReset;
import org.nikolic.programm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PasswordResetRepository
 *
 * Repository für Password-Reset Operationen
 */
@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    // Finde aktiven (nicht verwendeten) Reset für User
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.used = false ORDER BY pr.createdAt DESC")
    Optional<PasswordReset> findActiveByUser(@Param("user") User user);

    // Finde letzten Reset für User
    Optional<PasswordReset> findTopByUserOrderByCreatedAtDesc(User user);

    // Finde alle Resets für User
    List<PasswordReset> findByUserOrderByCreatedAtDesc(User user);

    // Finde gültige (nicht abgelaufene, nicht verwendete) Resets
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.used = false AND pr.expiresAt > :now ORDER BY pr.createdAt DESC")
    List<PasswordReset> findValidByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // Finde Reset by Token
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.resetToken = :token AND pr.used = false AND pr.tokenExpiresAt > :now")
    Optional<PasswordReset> findByValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Lösche alte verwendete Resets
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.used = true AND pr.usedAt < :cutoff")
    void deleteOldUsedResets(@Param("cutoff") LocalDateTime cutoff);

    // Lösche alte abgelaufene, nicht verwendete Resets
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.used = false AND pr.expiresAt < :cutoff")
    void deleteExpiredResets(@Param("cutoff") LocalDateTime cutoff);

    // Markiere alle Resets eines Users als verwendet (Sicherheit)
    @Modifying
    @Query("UPDATE PasswordReset pr SET pr.used = true, pr.usedAt = :now WHERE pr.user = :user AND pr.used = false")
    void markAllAsUsedForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // Zähle unverbrauchte Resets
    long countByUserAndUsedFalse(User user);

    // Finde Resets mit vielen Versuchen (für Security-Monitoring)
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.used = false AND pr.attempts >= :maxAttempts")
    List<PasswordReset> findWithExceededAttempts(@Param("maxAttempts") int maxAttempts);

    // Finde Resets nach IP (für Security-Monitoring)
    List<PasswordReset> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
}