package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.QuizCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {
    Optional<QuizCategory> findByName(String name);
}