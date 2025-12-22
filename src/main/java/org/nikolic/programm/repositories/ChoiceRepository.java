package org.  nikolic.programm.repositories;

import org.nikolic.programm.entities.  Choice;
import org.springframework. data.jpa.repository.  JpaRepository;
import org.  springframework.data.jpa.repository.  Modifying;
import org.springframework.data.jpa.repository.  Query;
import org.springframework.  data.repository.query.Param;
import org.springframework.stereotype. Repository;

import java.util.  List;
import java.util. Optional;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    // Find all choices for a question (ordered by position)
    List<Choice> findByQuestionIdOrderByPositionAsc(Long questionId);

    // ✅ Fixed: Use @Query for fields with underscores
    @Query("SELECT c FROM Choice c WHERE c. question.id = :questionId AND c.is_correct = :isCorrect ORDER BY c.position ASC")
    List<Choice> findByQuestionIdAndIsCorrectOrderByPositionAsc(@Param("questionId") Long questionId, @Param("isCorrect") Boolean isCorrect);

    // Find only correct choices for a question
    @Query("SELECT c FROM Choice c WHERE c.question.id = :questionId AND c.is_correct = true ORDER BY c.position ASC")
    List<Choice> findCorrectChoicesByQuestionId(@Param("questionId") Long questionId);

    // Find only incorrect choices for a question
    @Query("SELECT c FROM Choice c WHERE c.question.id = :questionId AND c.is_correct = false ORDER BY c.position ASC")
    List<Choice> findIncorrectChoicesByQuestionId(@Param("questionId") Long questionId);

    // Count choices for a question
    long countByQuestionId(Long questionId);

    // ✅ Fixed: Use @Query for counting with underscore fields
    @Query("SELECT COUNT(c) FROM Choice c WHERE c. question.id = :questionId AND c.is_correct = :isCorrect")
    long countByQuestionIdAndIsCorrect(@Param("questionId") Long questionId, @Param("isCorrect") Boolean isCorrect);

    // Get max position for a question
    @Query("SELECT COALESCE(MAX(c. position), 0) FROM Choice c WHERE c.question.id = : questionId")
    Integer findMaxPositionByQuestionId(@Param("questionId") Long questionId);

    // Delete all choices for a question
    @Modifying
    @Query("DELETE FROM Choice c WHERE c.question.  id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    // Find choice by question and position
    Optional<Choice> findByQuestionIdAndPosition(Long questionId, Integer position);

    // Check if question has any correct choices
    @Query("SELECT COUNT(c) > 0 FROM Choice c WHERE c.question.id = :questionId AND c.is_correct = true")
    boolean hasCorrectChoices(@Param("questionId") Long questionId);

    // Update choice positions for a question
    @Modifying
    @Query("UPDATE Choice c SET c.position = c.position + 1 WHERE c.question. id = :questionId AND c.position >= :  fromPosition")
    void incrementPositionsFrom(@Param("questionId") Long questionId, @Param("fromPosition") Integer fromPosition);
}