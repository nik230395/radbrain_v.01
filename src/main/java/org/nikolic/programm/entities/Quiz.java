package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 500)
    private String description;

    private String category; // New field for quiz category

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // Reference to the creator of the quiz (User entity)

    private LocalDateTime createdAt;

    private Boolean isPublished;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions;
}