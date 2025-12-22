package org.nikolic.programm.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    private String fullname;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Integer quizzesCreated;
    private Integer attemptsCompleted;
}