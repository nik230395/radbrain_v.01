package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.AcceptableAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcceptableAnswerRepository extends JpaRepository<AcceptableAnswer, Long> {

    // Alle akzeptierten Antworten für eine Frage
    List<AcceptableAnswer> findByQuestionId(Long questionId);
}