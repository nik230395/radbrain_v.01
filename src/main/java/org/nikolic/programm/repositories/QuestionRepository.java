package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Find all questions for a quiz (ordered by position)
    List<Question> findByQuizIdOrderByPositionAsc(Long quizId);

    // Find questions by type
    List<Question> findByQtype(QuestionType qtype);

    // Find questions by quiz ID and type
    List<Question> findByQuizIdAndQtype(Long quizId, QuestionType qtype);

    // Count questions in a quiz
    long countByQuizId(Long quizId);

    // Custom query: Get questions with choices
    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.choices WHERE q.quiz.id = :quizId ORDER BY q.position")
    List<Question> findByQuizIdWithChoices(Long quizId);

    // Get max position for a quiz (useful when adding new questions)
    @Query("SELECT COALESCE(MAX(q.position), 0) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxPositionByQuizId(Long quizId);
}