package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.PasswordResetCode;
import org.nikolic.programm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    // Suche aktiven Code für einen User (noch nicht verwendet)
    Optional<PasswordResetCode> findFirstByUserAndUsedOrderByCreatedAtDesc(User user, Boolean used);

    // Codes eines Users auflisten
    List<PasswordResetCode> findByUser(User user);
}