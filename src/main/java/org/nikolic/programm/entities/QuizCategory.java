package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "quiz_categories")
public class QuizCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories")
    @ToString.Exclude
    private List<Quiz> quizzes;
}