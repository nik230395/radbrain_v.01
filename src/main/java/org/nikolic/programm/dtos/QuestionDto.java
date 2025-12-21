package org.nikolic.programm.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDto {
    private Long id;
    private String qtype;
    private String text;
    private String auxText;
    private Integer position;
    private List<ChoiceDto> choices;
}