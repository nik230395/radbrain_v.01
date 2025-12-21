package org.nikolic.programm.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailDto {
    private String email;

    private String code;
}