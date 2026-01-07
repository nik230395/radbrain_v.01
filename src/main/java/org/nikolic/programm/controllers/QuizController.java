package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.SubmitQuizRequest;
import jakarta.validation.Valid;
import org.nikolic.programm.dtos.ApiResponse;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.QuizNotFoundException;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.services.UserService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public Quiz Controller
 */
@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    /**
     * Get all published quizzes with pagination
     */
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<QuizDto>>> getPublishedQuizzes(Pageable pageable) {
        Page<QuizDto> quizzes = quizService.findAllPublishedPaged(pageable) //error
                .map(QuizMapper::toLightDto);

        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    /**
     * Get quiz by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDto>> getQuiz(@PathVariable Long id) {
        Quiz quiz = quizService.findByIdWithQuestions(id)
                .orElseThrow(() -> new QuizNotFoundException(id));

        QuizDto dto = QuizMapper.toDto(quiz);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Submit quiz answers
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitQuiz(
            @PathVariable Long id,
            @Valid @RequestBody SubmitQuizRequest request,
            Authentication authentication) {

        Quiz quiz = quizService.findById(id)
                .orElseThrow(() -> new QuizNotFoundException(id));

        User user = userService.getAuthenticatedUser(authentication).orElse(null);

        Map<Long, Object> answersMap = convertAnswersToMap(request.getAnswers());
        Map<String, Object> result = quizService.evaluateAndSaveAttempt(
                quiz, user, answersMap
        );

        return ResponseEntity.ok(
                ApiResponse.success("Quiz ausgewertet", result)
        );
    }
    /**
     * Convert List<AnswerSubmission> to Map<questionId, answer>
     * Handles both single and multiple choice questions
     */
    private Map<Long, Object> convertAnswersToMap(List<SubmitQuizRequest.AnswerSubmission> submissions) {
        Map<Long, Object> answersMap = new HashMap<>();

        // Group by questionId
        Map<Long, List<Long>> grouped = submissions.stream()
                .collect(Collectors.groupingBy(
                        SubmitQuizRequest.AnswerSubmission::getQuestionId,
                        Collectors.mapping(
                                SubmitQuizRequest.AnswerSubmission::getChoiceId,
                                Collectors.toList()
                        )
                ));

        // Convert to appropriate format
        grouped.forEach((questionId, choiceIds) -> {
            if (choiceIds.size() == 1) {
                // Single choice - store choiceId directly
                answersMap.put(questionId, choiceIds.get(0));
            } else {
                // Multiple choice - store as list
                answersMap.put(questionId, choiceIds);
            }
        });

        return answersMap;
    }

}