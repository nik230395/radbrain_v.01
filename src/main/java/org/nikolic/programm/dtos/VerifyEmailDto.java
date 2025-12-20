package org.nikolic.programm.dtos;

import lombok.Data;

@Data
public class VerifyEmailDto {
    private String email;

    private String code;
}