package org.nikolic.programm.services;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.repositories.QuestionRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Service zur Verwaltung von Quizfragen
@Service
@Transactional
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuizRepository quizRepository;

    // Neue Frage anlegen
    public Question createQuestion(Long quizId, QuestionType qtype, String text, String auxText) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz nicht gefunden"));
        Integer maxPosition = questionRepository.findMaxPositionByQuizId(quizId);
        if (maxPosition == null) maxPosition = 0;

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQtype(qtype);
        question.setText(text);
        question.setAuxText(auxText);
        question.setPosition(maxPosition + 1);

        return questionRepository.save(question);
    }

    // Alle Fragen eines Quizzes
    public List<Question> getQuestionsByQuiz(Long quizId) {
        return questionRepository.findByQuizIdOrderByPositionAsc(quizId);
    }

    // Frage nach ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    // Frage bearbeiten
    public Question updateQuestion(Long id, String text, String auxText) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Frage nicht gefunden"));
        question.setText(text);
        question.setAuxText(auxText);
        return questionRepository.save(question);
    }

    // Frage löschen
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}