package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long>, JpaSpecificationExecutor<Quiz> {

    // ========================================
    // PROJECTIONS FOR LIGHTWEIGHT QUERIES
    // ========================================

    /**
     * Lightweight projection for list views
     * Only fetches essential fields, not full entities
     */
    interface QuizListProjection {
        Long getId();
        String getTitle();
        String getCategory();
        Boolean getIsPublished();
        LocalDateTime getCreatedAt();
        Integer getQuestionCount();
    }

    /**
     * Quiz summary projection
     */
    interface QuizSummary {
        Long getId();
        String getTitle();
        String getDescription();
        Boolean getIsPublished();
    }

    // ========================================
    // PAGINATED QUERIES (for large datasets)
    // ========================================

    /**
     * Find all quizzes with pagination
     */
    Page<Quiz> findAll(Pageable pageable);

    /**
     * Find published quizzes with pagination
     */
    Page<Quiz> findByIsPublishedTrue(Pageable pageable);

    /**
     * Find quizzes by category with pagination
     */
    Page<Quiz> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    /**
     * Find quizzes by creator with pagination
     */
    Page<Quiz> findByCreatedById(Long userId, Pageable pageable);

    /**
     * Search quizzes by title with pagination
     */
    Page<Quiz> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // ========================================
    // OPTIMIZED QUERIES WITH PROJECTIONS
    // ========================================

    /**
     * Get quiz list projection (lightweight)
     */
    @Query("SELECT q.id as id, q.title as title, q.category as category, " +
            "q.isPublished as isPublished, q.createdAt as createdAt, " +
            "SIZE(q.questions) as questionCount " +
            "FROM Quiz q WHERE q.isPublished = true " +
            "ORDER BY q.createdAt DESC")
    List<QuizListProjection> findAllPublishedProjections();

    /**
     * Get paginated quiz projections
     */
    @Query("SELECT q.id as id, q.title as title, q.category as category, " +
            "q.isPublished as isPublished, q.createdAt as createdAt, " +
            "SIZE(q.questions) as questionCount " +
            "FROM Quiz q ORDER BY q.createdAt DESC")
    Page<QuizListProjection> findAllProjections(Pageable pageable);

    // ========================================
    // EAGER LOADING QUERIES (N+1 Prevention)
    // ========================================

    /**
     * Find quiz with eager-loaded questions and choices
     * Prevents N+1 query problem
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions quest " +
            "LEFT JOIN FETCH quest.choices " +
            "WHERE q.id = :id")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<Quiz> findByIdWithQuestionsAndChoices(@Param("id") Long id);

    /**
     * Find quiz with questions only (lighter than above)
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Batch fetch quizzes with questions for multiple IDs
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE q.id IN :ids")
    List<Quiz> findByIdInWithQuestions(@Param("ids") List<Long> ids);

    // ========================================
    // BASIC QUERIES (No redundancy)
    // ========================================

    /**
     * Find all published quizzes (non-paginated, use sparingly)
     */
    List<Quiz> findByIsPublishedTrue();

    /**
     * Find quizzes by category
     */
    List<Quiz> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * Find all quizzes ordered by creation date
     */
    List<Quiz> findAllByOrderByCreatedAtDesc();

    /**
     * Find published quizzes by category
     */
    List<Quiz> findByIsPublishedTrueAndCategory(String category);

    /**
     * Search by title (case-insensitive)
     */
    List<Quiz> findByTitleContainingIgnoreCase(String title);

    // ========================================
    // ADVANCED SEARCH & FILTERING
    // ========================================

    /**
     * Multi-criteria search with pagination
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "WHERE (:category IS NULL OR q.category = :category) " +
            "AND (:isPublished IS NULL OR q.isPublished = :isPublished) " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY q.createdAt DESC")
    Page<Quiz> searchQuizzes(
            @Param("category") String category,
            @Param("isPublished") Boolean isPublished,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    /**
     * Find quizzes created within date range
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE q.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY q.createdAt DESC")
    List<Quiz> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find quizzes with minimum question count
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE SIZE(q.questions) >= :minQuestions " +
            "ORDER BY SIZE(q.questions) DESC")
    List<Quiz> findQuizzesWithMinQuestions(@Param("minQuestions") int minQuestions);

    /**
     * Find empty quizzes (no questions)
     */
    @Query("SELECT q FROM Quiz q WHERE SIZE(q.questions) = 0")
    List<Quiz> findEmptyQuizzes();


    /**
     * Count published quizzes
     */
    long countByIsPublished(Boolean isPublished);

    /**
     * Count quizzes by category
     */
    long countByCategory(String category);

    /**
     * Count quizzes without category
     */
    long countByCategoryIsNull();

    /**
     * Count quizzes by creator
     */
    long countByCreatedById(Long userId);

    /**
     * Get category statistics
     */
    @Query("SELECT q.category, COUNT(q) as count, " +
            "SUM(CASE WHEN q.isPublished = true THEN 1 ELSE 0 END) as publishedCount " +
            "FROM Quiz q " +
            "WHERE q.category IS NOT NULL " +
            "GROUP BY q.category " +
            "ORDER BY count DESC")
    List<Object[]> getCategoryStatistics();

    /**
     * Get quiz creation statistics per day
     */
    @Query("SELECT DATE(q.createdAt) as date, COUNT(q) as count " +
            "FROM Quiz q " +
            "WHERE q.createdAt >= :since " +
            "GROUP BY DATE(q.createdAt) " +
            "ORDER BY date DESC")
    List<Object[]> getCreationStatistics(@Param("since") LocalDateTime since);

    /**
     * Average questions per quiz
     */
    @Query("SELECT AVG(SIZE(q.questions)) FROM Quiz q WHERE q.isPublished = true")
    Double getAverageQuestionsPerQuiz();

    /**
     * Find most recent quizzes
     */
    List<Quiz> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find most recent published quizzes
     */
    @Query("SELECT q FROM Quiz q WHERE q.isPublished = true ORDER BY q.createdAt DESC")
    List<Quiz> findTop10PublishedByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find most popular quizzes (by attempt count)
     */
    @Query("SELECT q, COUNT(qa) as attemptCount " +
            "FROM Quiz q " +
            "LEFT JOIN QuizAttempt qa ON qa.quiz = q " +
            "WHERE q.isPublished = true " +
            "GROUP BY q " +
            "ORDER BY attemptCount DESC")
    List<Object[]> findMostPopularQuizzes(Pageable pageable);

    // ========================================
    // BATCH OPERATIONS
    // ========================================

    /**
     * Find multiple quizzes by IDs
     */
    List<Quiz> findByIdIn(List<Long> ids);

    /**
     * Find quizzes by multiple categories
     */
    List<Quiz> findByCategoryIn(List<String> categories);

    /**
     * Bulk publish/unpublish by IDs
     */
    @Query("UPDATE Quiz q SET q.isPublished = :published WHERE q.id IN :ids")
    int bulkUpdatePublishStatus(@Param("ids") List<Long> ids, @Param("published") Boolean published);

    /**
     * Check if quiz exists with title (case-insensitive)
     */
    boolean existsByTitleIgnoreCase(String title);

    /**
     * Check if user has any quizzes
     */
    boolean existsByCreatedById(Long userId);

    /**
     * Find duplicate quiz titles
     */
    @Query("SELECT q.title, COUNT(q) as count " +
            "FROM Quiz q " +
            "GROUP BY q.title " +
            "HAVING COUNT(q) > 1")
    List<Object[]> findDuplicateTitles();

    // ========================================
    // ADMIN QUERIES
    // ========================================

    /**
     * Find quizzes pending review (unpublished with questions)
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE q.isPublished = false " +
            "AND SIZE(q.questions) > 0 " +
            "ORDER BY q.createdAt DESC")
    List<Quiz> findPendingReview();

    /**
     * Find incomplete quizzes (no questions)
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE SIZE(q.questions) = 0 " +
            "ORDER BY q.createdAt DESC")
    List<Quiz> findIncompleteQuizzes();

    /**
     * Find quizzes by creator email
     */
    @Query("SELECT q FROM Quiz q WHERE q.createdBy.email = :email ORDER BY q.createdAt DESC")
    List<Quiz> findByCreatorEmail(@Param("email") String email);
}