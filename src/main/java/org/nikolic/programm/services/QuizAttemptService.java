package org.nikolic.programm.services;

import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.QuizAttempt;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.QuizAttemptRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository,
                              QuizRepository quizRepository,
                              UserRepository userRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuizAttempt startQuizAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz nicht gefunden"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setStartedAt(LocalDateTime.now());

        return quizAttemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt completeQuizAttempt(Long attemptId, String answersJson, BigDecimal scorePct) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Versuch nicht gefunden"));

        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAnswersJson(answersJson);
        attempt.setScorePct(scorePct);

        return quizAttemptRepository.save(attempt);
    }

    public List<QuizAttempt> getAttemptsByUser(Long userId) {
        return quizAttemptRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }

    public List<QuizAttempt> getAttemptsByQuiz(Long quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }
}