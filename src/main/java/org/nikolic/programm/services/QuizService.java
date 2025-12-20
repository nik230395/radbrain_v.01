package org.nikolic.programm.services;

import org.nikolic.programm.entities.*;
import org.nikolic.programm.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    public QuizService(QuizRepository quizRepository,
                       UserRepository userRepository,
                       QuestionRepository questionRepository,
                       ChoiceRepository choiceRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    /**
     * Create a new quiz with questions and choices
     */
    public Quiz createQuiz(String title, String description, Long createdById, List<Map<String, Object>> questionsData) {
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + createdById));

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setIsPublished(false);
        quiz.setCreatedBy(creator);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz = quizRepository.save(quiz);

        // Add questions and choices to the quiz
        if (questionsData != null && !questionsData.isEmpty()) {
            List<Question> questions = attachQuestionsAndChoices(quiz, questionsData);
            quiz.setQuestions(questions);
        }

        return quizRepository.save(quiz);
    }

    private List<Question> attachQuestionsAndChoices(Quiz quiz, List<Map<String, Object>> questionsData) {
        List<Question> questions = new ArrayList<>();
        int positionCounter = 1;

        for (Map<String, Object> questionData : questionsData) {
            Question question = new Question();
            question.setQuiz(quiz);
            question.setText((String) questionData.get("text"));
            question.setQtype(QuestionType.valueOf((String) questionData.get("type")));
            question.setPosition(positionCounter++);

            // Map choices
            List<Map<String, Object>> choicesData = (List<Map<String, Object>>) questionData.get("choices");
            if (choicesData != null) {
                List<Choice> choices = choicesData.stream()
                        .map(choiceData -> createChoiceFromData(choiceData, question))
                        .collect(Collectors.toList());
                question.setChoices(choices);
            }
            questionRepository.save(question);  // Save question to establish relationships
            questions.add(question);
        }

        return questions;
    }

    private Choice createChoiceFromData(Map<String, Object> choiceData, Question question) {
        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText((String) choiceData.get("text"));
        choice.setIsCorrect((Boolean) choiceData.get("isCorrect"));
        return choice;
    }

    /**
     * Retrieve all published quizzes
     */
    public List<Quiz> getAllPublished() {
        return quizRepository.findAll().stream()
                .filter(Quiz::getIsPublished)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all quizzes (admin access)
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Get a quiz by its ID
     */
    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    /**
     * Update a quiz's metadata
     */
    public Quiz updateQuizMetadata(Long quizId, String title, String description) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + quizId));
        if (title != null) quiz.setTitle(title);
        if (description != null) quiz.setDescription(description);
        return quizRepository.save(quiz);
    }

    /**
     * Publish a quiz
     */
    public Quiz markAsPublished(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + quizId));
        quiz.setIsPublished(true);
        return quizRepository.save(quiz);
    }

    /**
     * Unpublish a quiz
     */
    public Quiz markAsPrivate(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found with ID: " + quizId));
        quiz.setIsPublished(false);
        return quizRepository.save(quiz);
    }

    /**
     * Delete a quiz
     */
    public void deleteQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalStateException("Quiz not found with ID: " + quizId));
        quizRepository.delete(quiz);
    }
}