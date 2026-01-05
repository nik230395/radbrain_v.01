package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.ContentType;
import org.nikolic.programm.entities.LearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningContentRepository extends JpaRepository<LearningContent, Long> {

    List<LearningContent> findByModuleIdOrderByDisplayOrderAsc(Long moduleId);

    List<LearningContent> findByContentType(ContentType contentType);

    @Query("SELECT lc FROM LearningContent lc WHERE lc.module.id = :moduleId ORDER BY lc.displayOrder ASC")
    List<LearningContent> findContentsByModuleId(Long moduleId);

    @Query("SELECT COUNT(lc) FROM LearningContent lc WHERE lc.module.id = :moduleId")
    long countByModuleId(Long moduleId);

    @Query("SELECT SUM(LENGTH(lc.contentData)) FROM LearningContent lc WHERE lc.module.id = :moduleId")
    Long getTotalContentLength(Long moduleId);
}