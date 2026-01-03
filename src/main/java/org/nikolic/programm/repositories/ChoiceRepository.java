package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ✅ FIXED ChoiceRepository
 *
 * Changes:
 * - Removed @Query annotations where not needed (Spring Data JPA can generate queries)
 * - Updated method names to use camelCase field names
 * - Added useful query methods
 */
@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    /**
     * Find all choices for a question, ordered by position
     * ✅ FIXED: Spring Data JPA can auto-generate this query now
     */
    List<Choice> findByQuestionIdOrderByPositionAsc(Long questionId);

    /**
     * Find choices by question and correctness status
     * ✅ FIXED: Uses camelCase field name 'isCorrect'
     */
    List<Choice> findByQuestionIdAndIsCorrectOrderByPositionAsc(Long questionId, Boolean isCorrect);

    /**
     * Find only correct choices for a question
     * ✅ FIXED: Simplified using Spring Data JPA naming convention
     */
    default List<Choice> findCorrectChoicesByQuestionId(Long questionId) {
        return findByQuestionIdAndIsCorrectOrderByPositionAsc(questionId, true);
    }

    /**
     * Count choices for a question
     */
    long countByQuestionId(Long questionId);

    /**
     * Count correct/incorrect choices for a question
     * ✅ FIXED: Uses camelCase field name 'isCorrect'
     */
    long countByQuestionIdAndIsCorrect(Long questionId, Boolean isCorrect);

    /**
     * Delete all choices for a question
     */
    void deleteByQuestionId(Long questionId);

    /**
     * Check if question has any choices
     */
    boolean existsByQuestionId(Long questionId);

    /**
     * Find maximum position for a question (for auto-positioning new choices)
     */
    @Query("SELECT MAX(c.position) FROM Choice c WHERE c.question.id = :questionId")
    Integer findMaxPositionByQuestionId(@Param("questionId") Long questionId);

    /**
     * Find choices for multiple questions at once (for N+1 prevention)
     */
    @Query("SELECT c FROM Choice c WHERE c.question.id IN :questionIds ORDER BY c.question.id, c.position")
    List<Choice> findByQuestionIdIn(@Param("questionIds") List<Long> questionIds);

    /**
     * Find all correct choices for multiple questions (for bulk operations)
     */
    @Query("SELECT c FROM Choice c WHERE c.question.id IN :questionIds AND c.isCorrect = true ORDER BY c.question.id, c.position")
    List<Choice> findCorrectChoicesByQuestionIdIn(@Param("questionIds") List<Long> questionIds);
}