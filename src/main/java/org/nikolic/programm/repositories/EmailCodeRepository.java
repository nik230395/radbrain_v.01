package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.EmailCode;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.CodePurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {
    // Find codes for a user + purpose ordered by creation time descending
    List<EmailCode> findByUserAndPurposeOrderByCreatedAtDesc(User user, CodePurpose purpose);
}