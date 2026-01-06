package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.QuizCategory;
import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data. repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizCategoryRepository extends JpaRepository<QuizCategory, Long> {

    // Finde Kategorie nach Name
    Optional<QuizCategory> findByName(String name);

    // Zähle Quizzes mit einem bestimmten Kategorie-String
    // WICHTIG: Quiz.category ist ein String, nicht category.id!
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category = :categoryName")
    long countQuizzesByCategory(@Param("categoryName") String categoryName);
//
//    // Find category by name (case-insensitive)
//    Optional<QuizCategory> findByNameIgnoreCase(String name);
//
//    // Check if category exists by name
//    boolean existsByName(String name);
//
//    // Check if category exists by name (case-insensitive)
//    boolean existsByNameIgnoreCase(String name);
//
//    // Find categories with published quizzes
//    @Query("SELECT DISTINCT qc FROM QuizCategory qc JOIN qc.quizzes q WHERE q.isPublished = true ORDER BY qc.name")
//    List<QuizCategory> findCategoriesWithPublishedQuizzes();
//
//    // Find categories ordered by name
//    List<QuizCategory> findAllByOrderByNameAsc();
//
//    // Count quizzes in category
//    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId")
//    long countQuizzesByCategory(@Param("categoryId") Long categoryId);
//
//    // Count published quizzes in category
//    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId AND q.isPublished = true")
//    long countPublishedQuizzesByCategory(@Param("categoryId") Long categoryId);
//
//    // Find categories with quiz counts
//    @Query("SELECT qc, COUNT(q) FROM QuizCategory qc LEFT JOIN qc.quizzes q GROUP BY qc ORDER BY qc.name")
//    List<Object[]> findCategoriesWithQuizCounts();
//
//    // Find categories containing text in name
//    @Query("SELECT qc FROM QuizCategory qc WHERE LOWER(qc.name) LIKE LOWER(CONCAT('%', : searchText, '%')) ORDER BY qc.name")
//    List<QuizCategory> findByNameContainingIgnoreCase(@Param("searchText") String searchText);
//
//    // Find empty categories (no quizzes)
//    @Query("SELECT qc FROM QuizCategory qc WHERE NOT EXISTS (SELECT q FROM Quiz q WHERE q.category = qc)")
//    List<QuizCategory> findEmptyCategories();
}