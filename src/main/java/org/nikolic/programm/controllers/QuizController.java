package org.nikolic.programm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.utils.QuizMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Public endpoints for taking quizzes (get quiz, submit answers).
 * list/published is provided by another controller to avoid duplicate mappings.
 */
@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public QuizController(QuizService quizService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/published")
    public ResponseEntity<?> getPublishedQuizzes() {
        List<Quiz> quizzes = quizService.findPublished();
        List<QuizDto> dtos = quizzes.stream()
                .map(QuizMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable Long id) {
        Optional<Quiz> q = quizService.findById(id);
        if (q.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","not_found"));
        QuizDto dto = QuizMapper.toDto(q.get());
        return ResponseEntity.ok(dto);
    }

    /**
     * Submit answers for a quiz.
     * Body: { "answers": { "<questionId>": <selected> } }
     * where <selected> is list of choice ids for MC questions or string for text answers.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) throws Exception {
        Optional<Quiz> qopt = quizService.findById(id);
        if (qopt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","not_found"));
        Quiz quiz = qopt.get();

        User user = null;
        if (auth != null) {
            String email = auth.getName();
            user = userRepository.findByEmail(email).orElse(null);
        }

        Object answersObj = body.get("answers");
        // convertValue to Map (keys likely String) then build Map<Long,Object>
        Map<String,Object> raw = objectMapper.convertValue(answersObj, Map.class);
        Map<Long,Object> structured = new HashMap<>();
        if (raw != null) {
            for (Map.Entry<String,Object> e : raw.entrySet()) {
                try {
                    Long qid = Long.parseLong(e.getKey());
                    structured.put(qid, e.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }

        Map<String,Object> result = quizService.evaluateAndSaveAttempt(quiz, user, structured);
        return ResponseEntity.ok(result);
    }
}