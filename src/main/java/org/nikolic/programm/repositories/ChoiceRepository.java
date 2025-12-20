package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    // Find all choices for a question (ordered by position)
    List<Choice> findByQuestionIdOrderByPositionAsc(Long questionId);

    // Find correct choices for a question
    List<Choice> findByQuestionIdAndIsCorrect(Long questionId, Boolean isCorrect);

    // Count choices for a question
    long countByQuestionId(Long questionId);

    // Get max position for a question (useful when adding new choices)
    @Query("SELECT COALESCE(MAX(c.position), 0) FROM Choice c WHERE c.question.id = :questionId")
    Integer findMaxPositionByQuestionId(@Param("questionId") Long questionId);

    // Delete all choices for a question (when recreating question)
    void deleteByQuestionId(Long questionId);
}