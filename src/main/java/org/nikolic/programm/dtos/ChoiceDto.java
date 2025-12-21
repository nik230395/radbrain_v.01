package org.nikolic.programm.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChoiceDto {
    private Long id;
    private String text;
    // do NOT expose isCorrect for public DTO
}