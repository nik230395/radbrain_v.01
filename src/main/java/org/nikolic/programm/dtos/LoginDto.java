package org.nikolic.programm.dtos;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}