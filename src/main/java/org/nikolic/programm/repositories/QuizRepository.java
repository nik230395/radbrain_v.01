package org.nikolic.programm.repositories;

import org.nikolic.programm. entities.Quiz;
import org. nikolic.programm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework. data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype. Repository;

import java.time.LocalDateTime;
import java. util.List;
import java. util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Find published quizzes
    List<Quiz> findByIsPublishedTrueOrderByCreatedAtDesc();

    // Find unpublished quizzes
    List<Quiz> findByIsPublishedFalseOrderByCreatedAtDesc();

    // Find quizzes by creator
    List<Quiz> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    // Find published quizzes by creator
    List<Quiz> findByCreatedByAndIsPublishedTrueOrderByCreatedAtDesc(User createdBy);

    // Find quizzes by category
    @Query("SELECT q FROM Quiz q WHERE q.category.id = :categoryId ORDER BY q.createdAt DESC")
    List<Quiz> findByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId);

    // Find published quizzes by category
    @Query("SELECT q FROM Quiz q WHERE q.category.id = :categoryId AND q.isPublished = true ORDER BY q.createdAt DESC")
    List<Quiz> findPublishedByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId);

    // Search quizzes by title or description
    @Query("SELECT q FROM Quiz q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY q.createdAt DESC")
    List<Quiz> findByTitleContainingOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Search published quizzes only
    @Query("SELECT q FROM Quiz q WHERE q.isPublished = true AND (LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY q.createdAt DESC")
    List<Quiz> findPublishedByTitleContainingOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Count quizzes by publication status
    long countByIsPublished(Boolean isPublished);

    // Count quizzes by creator
    long countByCreatedBy(User createdBy);

    // Find quizzes created after a certain date
    List<Quiz> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    // Find popular quizzes (with attempts)
    @Query("SELECT q FROM Quiz q WHERE q.isPublished = true AND EXISTS (SELECT qa FROM QuizAttempt qa WHERE qa.quiz = q) ORDER BY (SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.quiz = q) DESC")
    List<Quiz> findPopularPublishedQuizzes();

    // Find quizzes with questions
    @Query("SELECT DISTINCT q FROM Quiz q JOIN FETCH q.questions WHERE q.isPublished = true ORDER BY q.createdAt DESC")
    List<Quiz> findPublishedQuizzesWithQuestions();

    // Find quiz by ID with full details (questions and choices)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions qu LEFT JOIN FETCH qu.choices WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestionsAndChoices(@Param("id") Long id);

    // Find recent quizzes (last N days)
    @Query("SELECT q FROM Quiz q WHERE q.isPublished = true AND q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quiz> findRecentPublishedQuizzes(@Param("since") LocalDateTime since);

    // Find quizzes without questions (for validation)
    @Query("SELECT q FROM Quiz q WHERE NOT EXISTS (SELECT qu FROM Question qu WHERE qu.quiz = q)")
    List<Quiz> findQuizzesWithoutQuestions();

    // Find quizzes ready for publishing (has questions)
    @Query("SELECT q FROM Quiz q WHERE q.isPublished = false AND EXISTS (SELECT qu FROM Question qu WHERE qu.quiz = q)")
    List<Quiz> findUnpublishedQuizzesWithQuestions();
}