package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.AcceptableAnswer;
import org.nikolic.programm.entities.MatchMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ✅ FIXED AcceptableAnswerRepository
 *
 * Changes:
 * - Removed unnecessary @Query annotations
 * - Updated method names to use camelCase field names (matchMode, answerText)
 * - Spring Data JPA can now auto-generate these queries
 */
@Repository
public interface AcceptableAnswerRepository extends JpaRepository<AcceptableAnswer, Long> {

    /**
     * Find all acceptable answers for a question
     * ✅ FIXED: Spring Data JPA auto-generates this query
     */
    List<AcceptableAnswer> findByQuestionId(Long questionId);

    /**
     * Find acceptable answers by question and match mode
     * ✅ FIXED: Uses camelCase field name 'matchMode'
     */
    List<AcceptableAnswer> findByQuestionIdAndMatchMode(Long questionId, MatchMode matchMode);

    /**
     * Find acceptable answers by match mode only
     * ✅ FIXED: Uses camelCase field name 'matchMode'
     */
    List<AcceptableAnswer> findByMatchMode(MatchMode matchMode);

    /**
     * Count acceptable answers for a question
     */
    long countByQuestionId(Long questionId);

    /**
     * Delete all acceptable answers for a question
     */
    void deleteByQuestionId(Long questionId);

    /**
     * Check if question has any acceptable answers
     */
    boolean existsByQuestionId(Long questionId);

    /**
     * Find acceptable answers containing specific text (case-insensitive search)
     * ✅ FIXED: Uses camelCase field name 'answerText'
     */
    List<AcceptableAnswer> findByAnswerTextContainingIgnoreCase(String searchText);

    /**
     * Find acceptable answers for multiple questions at once (N+1 prevention)
     */
    @Query("SELECT aa FROM AcceptableAnswer aa WHERE aa.question.id IN :questionIds ORDER BY aa.question.id")
    List<AcceptableAnswer> findByQuestionIdIn(@Param("questionIds") List<Long> questionIds);

    /**
     * Find exact match answers for a question
     */
    default List<AcceptableAnswer> findExactMatchAnswers(Long questionId) {
        return findByQuestionIdAndMatchMode(questionId, MatchMode.EXACT);
    }

    /**
     * Find case-insensitive match answers for a question
     */
    default List<AcceptableAnswer> findCaseInsensitiveAnswers(Long questionId) {
        return findByQuestionIdAndMatchMode(questionId, MatchMode.CASE_INSENSITIVE);
    }

    /**
     * Find contains match answers for a question
     */
    default List<AcceptableAnswer> findContainsAnswers(Long questionId) {
        return findByQuestionIdAndMatchMode(questionId, MatchMode.CONTAINS);
    }

    /**
     * Find regex match answers for a question
     */
    default List<AcceptableAnswer> findRegexAnswers(Long questionId) {
        return findByQuestionIdAndMatchMode(questionId, MatchMode.REGEX);
    }
}