package org.nikolic.programm.services;

import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.QuestionType;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.repositories.QuestionRepository;
import org.nikolic.programm.repositories.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public Question createQuestion(Long quizId, QuestionType qtype, String text, String auxText) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz nicht gefunden"));

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQtype(qtype);
        question.setText(text);
        question.setAuxText(auxText);

        // Position automatisch setzen
        List<Question> existing = questionRepository.findByQuizIdOrderByPositionAsc(quizId);
        question.setPosition(existing.size());

        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByQuiz(Long quizId) {
        return questionRepository.findByQuizIdOrderByPositionAsc(quizId);
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    @Transactional
    public Question updateQuestion(Long id, String text, String auxText) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Frage nicht gefunden"));

        if (text != null) {
            question.setText(text);
        }
        if (auxText != null) {
            question.setAuxText(auxText);
        }

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Frage nicht gefunden");
        }
        questionRepository.deleteById(id);
    }
}