package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.AcceptableAnswer;
import org. nikolic.programm.entities. MatchMode;
import org.springframework. data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository. Modifying;
import org.springframework.data.jpa.repository. Query;
import org.springframework. data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcceptableAnswerRepository extends JpaRepository<AcceptableAnswer, Long> {

    // Find all acceptable answers for a question
    List<AcceptableAnswer> findByQuestionId(Long questionId);

    // ✅ Fixed: Use @Query for fields with underscores
    @Query("SELECT aa FROM AcceptableAnswer aa WHERE aa.question.id = :questionId AND aa.match_mode = :matchMode")
    List<AcceptableAnswer> findByQuestionIdAndMatchMode(@Param("questionId") Long questionId, @Param("matchMode") MatchMode matchMode);

    // Count acceptable answers for a question
    long countByQuestionId(Long questionId);

    // Delete all acceptable answers for a question
    void deleteByQuestionId(Long questionId);

    // ✅ Fixed: Use @Query for text search with underscore field
    @Query("SELECT aa FROM AcceptableAnswer aa WHERE aa.question.id = :questionId AND LOWER(aa.answer_text) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<AcceptableAnswer> findByQuestionIdAndAnswerTextContainingIgnoreCase(@Param("questionId") Long questionId, @Param("text") String text);
}