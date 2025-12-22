package org. nikolic.programm.dtos;

public class UpdateQuestionRequest {

    private String text;
    private String auxText;
    private Integer position;

    // Constructors
    public UpdateQuestionRequest() {}

    public UpdateQuestionRequest(String text, String auxText, Integer position) {
        this.text = text;
        this. auxText = auxText;
        this.position = position;
    }

    // Getters and Setters
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
}