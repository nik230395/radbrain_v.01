package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Alle veröffentlichten Quizzes
    List<Quiz> findByIsPublishedTrue();

    // Quizzes eines bestimmten Erstellers
    List<Quiz> findByCreatedById(Long userId);

    // Quizzes nach Kategorie-String sortiert nach Erstellungsdatum
    // WICHTIG: category ist ein String, NICHT ein Objekt mit id!
    List<Quiz> findByCategoryOrderByCreatedAtDesc(String category);

    // Alle Quizzes sortiert nach Erstellungsdatum
    List<Quiz> findAllByOrderByCreatedAtDesc();

    // Quiz mit eager-loaded Questions holen (verhindert N+1 Problem)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    // Veröffentlichte Quizzes nach Kategorie
    List<Quiz> findByIsPublishedTrueAndCategory(String category);

    // Suche nach Titel (case-insensitive)
    List<Quiz> findByTitleContainingIgnoreCase(String title);
}