package org.nikolic.programm.dtos;

public class StatisticsDto {

    private long totalUsers;
    private long totalQuizzes;
    private long totalAttempts;

    public StatisticsDto(long totalUsers, long totalQuizzes, long totalAttempts) {
        this.totalUsers = totalUsers;
        this.totalQuizzes = totalQuizzes;
        this.totalAttempts = totalAttempts;
    }

    // Getters and setters
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
}