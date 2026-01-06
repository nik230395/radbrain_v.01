package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.springframework.data.jpa.repository. JpaRepository;
import org. springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa. repository.Query;
import org. springframework.data.repository.query. Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByPositionAsc(Long quizId);

    // Find questions by type
    List<Question> findByQtypeOrderByIdDesc(QuestionType qtype);

    // Find questions by quiz ID and type
    List<Question> findByQuizIdAndQtypeOrderByPositionAsc(Long quizId, QuestionType qtype);

    // Count questions in a quiz
    long countByQuizId(Long quizId);

    // Count questions by type in a quiz
    long countByQuizIdAndQtype(Long quizId, QuestionType qtype);

    // Find questions with choices (optimized for quiz taking)
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.choices c WHERE q.quiz.id = :quizId ORDER BY q.position, c.position")
    List<Question> findByQuizIdWithChoicesOrderedByPosition(@Param("quizId") Long quizId);

    // Find questions with acceptable answers (for text-based questions)
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.acceptableAnswers WHERE q.quiz.id = :quizId AND q.qtype IN ('SHORT_TEXT', 'FILL_GAP') ORDER BY q.position")
    List<Question> findTextQuestionsByQuizIdWithAnswers(@Param("quizId") Long quizId);

    // Get max position for a quiz
    @Query("SELECT COALESCE(MAX(q.position), 0) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxPositionByQuizId(@Param("quizId") Long quizId);

    // Find question by quiz and position
    Optional<Question> findByQuizIdAndPosition(Long quizId, Integer position);

    // Find questions containing specific text
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND LOWER(q.text) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY q.position")
    List<Question> findByQuizIdAndTextContainingIgnoreCase(@Param("quizId") Long quizId, @Param("searchText") String searchText);

    // Update question positions for a quiz
    @Modifying
    @Query("UPDATE Question q SET q.position = q.position + 1 WHERE q.quiz.id = :quizId AND q.position >= : fromPosition")
    void incrementPositionsFrom(@Param("quizId") Long quizId, @Param("fromPosition") Integer fromPosition);

    // Delete all questions for a quiz
    @Modifying
    @Query("DELETE FROM Question q WHERE q.quiz. id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);

    // Find questions without choices (for validation)
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.qtype IN ('SINGLE', 'MULTIPLE', 'TRUE_FALSE') AND NOT EXISTS (SELECT c FROM Choice c WHERE c.question = q)")
    List<Question> findChoiceQuestionsWithoutChoices(@Param("quizId") Long quizId);

    // Find questions without acceptable answers (for validation)
    @Query("SELECT q FROM Question q WHERE q.quiz.id = : quizId AND q.qtype IN ('SHORT_TEXT', 'FILL_GAP') AND NOT EXISTS (SELECT aa FROM AcceptableAnswer aa WHERE aa.question = q)")
    List<Question> findTextQuestionsWithoutAnswers(@Param("quizId") Long quizId);
}