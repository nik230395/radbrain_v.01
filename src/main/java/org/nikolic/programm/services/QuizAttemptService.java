package org.nikolic.programm.services;

import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Service für Quiz-Versuche
@Service
@Transactional
public class QuizAttemptService {
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private UserRepository userRepository;

    // Quizversuch starten
    public QuizAttempt startQuizAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz nicht gefunden"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));
        if (quiz.getIsPublished() == null || !quiz.getIsPublished()) {
            throw new RuntimeException("Quiz ist nicht veröffentlicht!");
        }
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setScorePct(BigDecimal.ZERO);
        return quizAttemptRepository.save(attempt);
    }

    // Versuch abschließen
    public QuizAttempt completeQuizAttempt(Long attemptId, String answersJson, BigDecimal scorePct) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz-Versuch nicht gefunden"));
        if (attempt.getCompletedAt() != null) {
            throw new RuntimeException("Quiz-Versuch bereits abgeschlossen!");
        }
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAnswersJson(answersJson);
        attempt.setScorePct(scorePct);
        return quizAttemptRepository.save(attempt);
    }

    // Alle Versuche eines Users
    public List<QuizAttempt> getAttemptsByUser(Long userId) {
        return quizAttemptRepository.findByUserIdOrderByStartedAtDesc(userId);
    }
}