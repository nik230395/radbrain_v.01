package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {

    List<LearningModule> findByLearningAreaIdOrderByDisplayOrderAsc(Long learningAreaId);

    @Query("SELECT lm FROM LearningModule lm LEFT JOIN FETCH lm.contents WHERE lm.id = :id")
    Optional<LearningModule> findByIdWithContents(Long id);

    @Query("SELECT lm FROM LearningModule lm WHERE lm.learningArea.id = :areaId ORDER BY lm.displayOrder ASC")
    List<LearningModule> findModulesByAreaId(Long areaId);

    @Query("SELECT COUNT(lm) FROM LearningModule lm WHERE lm.learningArea.id = :areaId")
    long countByLearningAreaId(Long areaId);
}