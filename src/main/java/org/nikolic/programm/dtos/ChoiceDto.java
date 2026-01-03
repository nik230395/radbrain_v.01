package org.nikolic.programm.dtos;

/**
 * ✅ FIXED ChoiceDto
 * Now includes isCorrect field!
 */
public class ChoiceDto {
    private Long id;
    private String text;
    private Integer position;
    private Boolean isCorrect;  // ✅ ADDED THIS!

    // Constructors
    public ChoiceDto() {}

    public ChoiceDto(Long id, String text, Integer position, Boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.position = position;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    // Alternative getter for Jackson (handles both isCorrect and is_correct)
    public Boolean isCorrect() {
        return isCorrect;
    }
}