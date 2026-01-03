package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.QuizAttempt;
import org.springframework.data.jpa.repository. JpaRepository;
import org. springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query. Param;
import org.springframework. stereotype.Repository;

import java. math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserId(Long userId);
    List<QuizAttempt> findByQuizId(Long quizId);
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);
    // Find all attempts by a user (most recent first)
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    // Find all attempts for a specific quiz
    List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(Long quizId);

    // Find user's attempts for a specific quiz
    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);

    // Find completed attempts only
    List<QuizAttempt> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long userId);

    // Find incomplete attempts (started but not completed)
    List<QuizAttempt> findByUserIdAndCompletedAtIsNullOrderByStartedAtDesc(Long userId);

    // Get user's best score for a quiz
    @Query("SELECT MAX(qa. scorePct) FROM QuizAttempt qa WHERE qa.user. id = :userId AND qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL")
    Optional<BigDecimal> findBestScoreByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    // Get user's latest score for a quiz
    @Query("SELECT qa.scorePct FROM QuizAttempt qa WHERE qa.user. id = :userId AND qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL ORDER BY qa.completedAt DESC LIMIT 1")
    Optional<BigDecimal> findLatestScoreByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    // Get average score for a quiz
    @Query("SELECT AVG(qa.scorePct) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL")
    Optional<BigDecimal> findAverageScoreByQuiz(@Param("quizId") Long quizId);

    // Count total attempts for a quiz
    long countByQuizId(Long quizId);

    // Count completed attempts for a quiz
    long countByQuizIdAndCompletedAtIsNotNull(Long quizId);

    // Count completed attempts for a user
    long countByUserIdAndCompletedAtIsNotNull(Long userId);

    // Count attempts by user for specific quiz
    long countByUserIdAndQuizId(Long userId, Long quizId);

    // Find recent attempts (last N days)
    List<QuizAttempt> findByUserIdAndStartedAtAfterOrderByStartedAtDesc(Long userId, LocalDateTime since);

    // Find attempts within date range
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.startedAt BETWEEN :startDate AND :endDate ORDER BY qa.startedAt DESC")
    List<QuizAttempt> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find top scores for a quiz
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.completedAt IS NOT NULL ORDER BY qa.scorePct DESC, qa.completedAt ASC")
    List<QuizAttempt> findTopScoresByQuiz(@Param("quizId") Long quizId);

    // Find user's incomplete attempt for a quiz (to resume)
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user. id = :userId AND qa.quiz.id = :quizId AND qa.completedAt IS NULL ORDER BY qa.startedAt DESC LIMIT 1")
    Optional<QuizAttempt> findIncompleteAttemptByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    // Get completion rate for a quiz
    @Query("SELECT CAST(COUNT(CASE WHEN qa.completedAt IS NOT NULL THEN 1 END) AS double) / COUNT(*) FROM QuizAttempt qa WHERE qa.quiz.id = : quizId")
    Optional<Double> findCompletionRateByQuiz(@Param("quizId") Long quizId);

    // Find attempts with scores above threshold
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.scorePct >= :minScore AND qa.completedAt IS NOT NULL ORDER BY qa.scorePct DESC")
    List<QuizAttempt> findAttemptsByQuizAndMinScore(@Param("quizId") Long quizId, @Param("minScore") BigDecimal minScore);

    // Statistics:  attempts per day
    @Query("SELECT DATE(qa.startedAt) as date, COUNT(qa) FROM QuizAttempt qa WHERE qa.startedAt >= :since GROUP BY DATE(qa.startedAt) ORDER BY date DESC")
    List<Object[]> findAttemptsPerDayStats(@Param("since") LocalDateTime since);

    // Find user's best attempts across all quizzes
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.completedAt IS NOT NULL AND qa.scorePct = (SELECT MAX(qa2.scorePct) FROM QuizAttempt qa2 WHERE qa2.user.id = qa.user.id AND qa2.quiz.id = qa.quiz. id AND qa2.completedAt IS NOT NULL) ORDER BY qa.scorePct DESC")
    List<QuizAttempt> findUsersBestAttempts(@Param("userId") Long userId);

    long countByStartedAtAfter(LocalDateTime date);
    List<QuizAttempt> findTop20ByOrderByStartedAtDesc();
}