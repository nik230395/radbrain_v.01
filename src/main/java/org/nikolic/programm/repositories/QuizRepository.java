package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Ensure no parameter in method signature
    List<Quiz> findByIsPublishedTrue();
    
    // Check if a quiz with the given title exists for a specific user
    boolean existsByTitleIgnoreCaseAndCreatedBy(String title, User user);
}