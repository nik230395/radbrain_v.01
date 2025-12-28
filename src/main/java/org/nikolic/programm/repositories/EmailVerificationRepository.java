package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.EmailVerification;
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
 * EmailVerificationRepository
 *
 * Repository für Email-Verification Operationen
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // Finde Verification by User
    Optional<EmailVerification> findByUser(User user);

    // Finde Verification by User ID
    Optional<EmailVerification> findByUserId(Long userId);

    // Finde alle unverifizierten
    List<EmailVerification> findByVerifiedFalse();

    // Finde alle abgelaufenen, unverifizierten
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verified = false AND ev.expiresAt < :now")
    List<EmailVerification> findExpiredUnverified(@Param("now") LocalDateTime now);

    // Lösche alle abgelaufenen, unverifizierten
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.verified = false AND ev.expiresAt < :cutoff")
    void deleteExpiredUnverified(@Param("cutoff") LocalDateTime cutoff);

    // Zähle unverified
    long countByVerifiedFalse();

    // Zähle verified
    long countByVerifiedTrue();

    // Finde nach Code (für Admin-Suche)
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verificationCode = :code")
    List<EmailVerification> findByCode(@Param("code") String code);

    // Finde Verifications mit vielen Versuchen
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verified = false AND ev.attempts >= :maxAttempts")
    List<EmailVerification> findWithExceededAttempts(@Param("maxAttempts") int maxAttempts);

    // Cleanup alte verifizierte Einträge (optional)
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.verified = true AND ev.verifiedAt < :cutoff")
    void deleteOldVerified(@Param("cutoff") LocalDateTime cutoff);
}