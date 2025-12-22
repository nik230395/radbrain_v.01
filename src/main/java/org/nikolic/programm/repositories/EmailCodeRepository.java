package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.EmailCode;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.CodePurpose;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository. Modifying;
import org.springframework.data.jpa.repository. Query;
import org.springframework. data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

    // Find codes for a user + purpose ordered by creation time descending
    List<EmailCode> findByUserAndPurposeOrderByCreatedAtDesc(User user, CodePurpose purpose);

    // Find latest unused code for user and purpose
    @Query("SELECT ec FROM EmailCode ec WHERE ec.user = :user AND ec.purpose = : purpose AND ec.consumedAt IS NULL ORDER BY ec. createdAt DESC LIMIT 1")
    Optional<EmailCode> findLatestUnusedCode(@Param("user") User user, @Param("purpose") CodePurpose purpose);

    // Find valid (unexpired and unused) codes
    @Query("SELECT ec FROM EmailCode ec WHERE ec. user = :user AND ec.purpose = :purpose AND ec.consumedAt IS NULL AND ec.expiresAt > :now ORDER BY ec.createdAt DESC")
    List<EmailCode> findValidCodes(@Param("user") User user, @Param("purpose") CodePurpose purpose, @Param("now") LocalDateTime now);

    // Clean up expired codes
    @Modifying
    @Query("DELETE FROM EmailCode ec WHERE ec.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    // Mark all codes as consumed for user and purpose
    @Modifying
    @Query("UPDATE EmailCode ec SET ec.consumedAt = : now WHERE ec.user = :user AND ec.purpose = :purpose AND ec.consumedAt IS NULL")
    void markAllAsConsumed(@Param("user") User user, @Param("purpose") CodePurpose purpose, @Param("now") LocalDateTime now);

    // Count unused codes for user
    long countByUserAndPurposeAndConsumedAtIsNull(User user, CodePurpose purpose);

    // Find codes by user and date range
    List<EmailCode> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);
}