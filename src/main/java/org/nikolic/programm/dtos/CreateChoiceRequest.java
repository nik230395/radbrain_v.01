package org. nikolic.programm.dtos;

public class CreateChoiceRequest {

    private Long questionId;
    private String text;
    private Boolean isCorrect;
    private Integer position;

    // Constructors
    public CreateChoiceRequest() {}

    public CreateChoiceRequest(Long questionId, String text, Boolean isCorrect, Integer position) {
        this.questionId = questionId;
        this.text = text;
        this.isCorrect = isCorrect;
        this.position = position;
    }

    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}