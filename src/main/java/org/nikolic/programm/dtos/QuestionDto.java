package org.nikolic.programm.dtos;

import java.util.List;

public class QuestionDto {
    private Long id;
    private String qtype;
    private String text;
    private String auxText;
    private Integer position;
    private List<ChoiceDto> choices;

    // Constructors
    public QuestionDto() {}

    public QuestionDto(Long id, String qtype, String text, String auxText, Integer position, List<ChoiceDto> choices) {
        this.id = id;
        this.qtype = qtype;
        this.text = text;
        this.auxText = auxText;
        this.position = position;
        this.choices = choices;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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