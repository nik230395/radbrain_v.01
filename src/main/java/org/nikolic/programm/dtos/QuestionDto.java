package org.nikolic.programm.dtos;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String qtype;
    private String text;
    private String auxText;
    private Integer position;
    private List<ChoiceDto> choices;
}