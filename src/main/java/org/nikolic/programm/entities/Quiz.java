package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Quiz-ID

    @Column(nullable = false)
    private String title; // Titel des Quiz

    @Column(columnDefinition = "TEXT")
    private String description; // Beschreibung zum Quiz

    @Column(name = "is_published")
    private Boolean isPublished; // Veröffentlicht (öffentlich) oder nicht

    @ManyToOne
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    private User createdBy; // Autor / Ersteller

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Erstellungsdatum

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Question> questions; // Fragen des Quiz

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<QuizAttempt> attempts; // Alle Versuche zu diesem Quiz

    @ManyToMany
    @JoinTable(
            name = "quiz_category_mapping",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @ToString.Exclude
    private List<QuizCategory> categories;
}