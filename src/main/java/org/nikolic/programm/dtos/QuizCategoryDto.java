package org.nikolic.programm.dtos;

public class QuizCategoryDto {
    private Long id;
    private String name;
    private Integer quizCount; // Number of quizzes in this category

    // Constructors
    public QuizCategoryDto() {}

    public QuizCategoryDto(Long id, String name, Integer quizCount) {
        this.id = id;
        this.name = name;
        this.quizCount = quizCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Integer quizCount) {
        this.quizCount = quizCount;
    }
}