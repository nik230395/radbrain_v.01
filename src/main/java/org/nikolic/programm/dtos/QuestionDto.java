package org.nikolic.programm.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDto {
    private Long id;
    private String text;
    private String qtype;
    private String auxText;
    private Integer position;
    private List<ChoiceDto> choices;

    private QuestionDto() {}

    // Getters
    public Long getId() { return id; }
    public String getText() { return text; }
    public String getQtype() { return qtype; }
    public String getAuxText() { return auxText; }
    public Integer getPosition() { return position; }
    public List<ChoiceDto> getChoices() {
        return choices != null ? new ArrayList<>(choices) : Collections.emptyList();
    }

    // Setters (for Jackson)
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setQtype(String qtype) { this.qtype = qtype; }
    public void setAuxText(String auxText) { this.auxText = auxText; }
    public void setPosition(Integer position) { this.position = position; }
    public void setChoices(List<ChoiceDto> choices) { this.choices = choices; }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final QuestionDto dto = new QuestionDto();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder text(String text) {
            dto.text = text;
            return this;
        }

        public Builder qtype(String qtype) {
            dto.qtype = qtype;
            return this;
        }

        public Builder auxText(String auxText) {
            dto.auxText = auxText;
            return this;
        }

        public Builder position(Integer position) {
            dto.position = position;
            return this;
        }

        public Builder choices(List<ChoiceDto> choices) {
            dto.choices = choices != null ? new ArrayList<>(choices) : new ArrayList<>();
            return this;
        }

        public QuestionDto build() {
            return dto;
        }
    }
}