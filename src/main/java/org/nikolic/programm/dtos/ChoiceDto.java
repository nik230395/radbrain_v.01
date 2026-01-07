package org.nikolic.programm.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceDto {
    private Long id;
    private String text;
    private Integer position;
    private Boolean isCorrect;

    private ChoiceDto() {}

    // Getters
    public Long getId() { return id; }
    public String getText() { return text; }
    public Integer getPosition() { return position; }
    public Boolean getIsCorrect() { return isCorrect; }

    // Setters (for Jackson)
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setPosition(Integer position) { this.position = position; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ChoiceDto dto = new ChoiceDto();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder text(String text) {
            dto.text = text;
            return this;
        }

        public Builder position(Integer position) {
            dto.position = position;
            return this;
        }

        public Builder isCorrect(Boolean isCorrect) {
            dto.isCorrect = isCorrect;
            return this;
        }

        public ChoiceDto build() {
            return dto;
        }
    }
}