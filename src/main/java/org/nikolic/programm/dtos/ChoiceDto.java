package org. nikolic.programm.dtos;

public class ChoiceDto {
    private Long id;
    private String text;
    private Integer position;
    // isCorrect is intentionally NOT exposed for security reasons

    // Constructors
    public ChoiceDto() {}

    public ChoiceDto(Long id, String text, Integer position) {
        this.id = id;
        this.text = text;
        this.position = position;
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
}