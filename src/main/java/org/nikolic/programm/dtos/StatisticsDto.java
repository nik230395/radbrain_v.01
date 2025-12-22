package org.nikolic.programm. dtos;

public class StatisticsDto {
    private long totalUsers;
    private long totalQuizzes;
    private long totalAttempts;
    private long publishedQuizzes;
    private long activeUsers;

    // Constructors
    public StatisticsDto() {}

    public StatisticsDto(long totalUsers, long totalQuizzes, long totalAttempts,
                         long publishedQuizzes, long activeUsers) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this. totalAttempts = totalAttempts;
        this.publishedQuizzes = publishedQuizzes;
        this.activeUsers = activeUsers;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public long getPublishedQuizzes() {
        return publishedQuizzes;
    }

    public void setPublishedQuizzes(long publishedQuizzes) {
        this.publishedQuizzes = publishedQuizzes;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
}