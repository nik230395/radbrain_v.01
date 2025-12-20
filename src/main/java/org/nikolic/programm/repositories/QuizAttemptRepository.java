package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Find all attempts by a user
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    // Find all attempts for a specific quiz
    List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(Long quizId);

    // Find user's attempts for a specific quiz
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);

    // Find completed attempts only
    List<QuizAttempt> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long userId);

    // Find incomplete attempts (started but not completed)
    List<QuizAttempt> findByUserIdAndCompletedAtIsNull(Long userId);

    // Get user's best score for a quiz
    @Query("SELECT MAX(qa.scorePct) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL")
    Optional<Double> findBestScoreByUserAndQuiz(Long userId, Long quizId);

    // Get average score for a quiz
    @Query("SELECT AVG(qa.scorePct) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL")
    Optional<Double> findAverageScoreByQuiz(Long quizId);

    // Count total attempts for a quiz
    long countByQuizId(Long quizId);

    // Count completed attempts for a user
    long countByUserIdAndCompletedAtIsNotNull(Long userId);

    // Find recent attempts (last N days)
    List<QuizAttempt> findByUserIdAndStartedAtAfterOrderByStartedAtDesc(Long userId, LocalDateTime since);
}