package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.LearningArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningAreaRepository extends JpaRepository<LearningArea, Long> {

    Optional<LearningArea> findBySlug(String slug);

    List<LearningArea> findByIsPublishedOrderByDisplayOrderAsc(Boolean isPublished);

    List<LearningArea> findAllByOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);

    @Query("SELECT la FROM LearningArea la LEFT JOIN FETCH la.modules WHERE la.id = :id")
    Optional<LearningArea> findByIdWithModules(Long id);

    @Query("SELECT la FROM LearningArea la LEFT JOIN FETCH la.modules WHERE la.slug = :slug")
    Optional<LearningArea> findBySlugWithModules(String slug);
}