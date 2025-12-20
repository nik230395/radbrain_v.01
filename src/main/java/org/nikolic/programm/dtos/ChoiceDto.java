package org.nikolic.programm.dtos;

import lombok.Data;

@Data
public class ChoiceDto {
    private Long id;
    private String text;
    // do NOT expose isCorrect for public DTO
}