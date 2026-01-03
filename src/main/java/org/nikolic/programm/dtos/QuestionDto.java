package org.nikolic.programm.dtos;

import java.util.List;

/**
 * QuestionDto with choices including isCorrect
 */
public class QuestionDto {
    private Long id;
    private String text;
    private String qtype;
    private String auxText;
    private Integer position;
    private List<ChoiceDto> choices;

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

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getAuxText() {
        return auxText;
    }

    public void setAuxText(String auxText) {
        this.auxText = auxText;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public List<ChoiceDto> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceDto> choices) {
        this.choices = choices;
    }
}