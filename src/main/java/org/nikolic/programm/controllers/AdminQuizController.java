package org.nikolic.programm.controllers;

import org.nikolic.programm.dtos.CreateQuizRequest;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Quiz;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.exceptions.ForbiddenException;
import org.nikolic.programm.exceptions.QuizAlreadyExistsException;
import org.nikolic.programm.exceptions.QuizNotFoundException;
import org.nikolic.programm.exceptions.UnauthorizedException;
import org.nikolic.programm.repositories.UserRepository;
import org.nikolic.programm.services.QuizService;
import org.nikolic.programm.utils.QuizMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller to manage quizzes.
 * Protected endpoints should be accessed with an Admin JWT.
 * 
 * Optimizations:
 * - Added comprehensive logging for debugging and error analysis
 * - Improved error handling with detailed messages and proper HTTP status codes
 * - Refactored authentication/authorization logic to eliminate redundant database calls
 * - Added null-safety checks and validation
 * - Custom exceptions for better error categorization
 */
@RestController
@RequestMapping("/api/secure/admin/quizzes")
@CrossOrigin(origins = "*")
public class AdminQuizController {

    private static final Logger logger = LoggerFactory.getLogger(AdminQuizController.class);

    private final QuizService quizService;
    private final UserRepository userRepository;

    public AdminQuizController(QuizService quizService, UserRepository userRepository) {
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    /**
     * Get authenticated user with admin validation in a single database call.
     * This eliminates redundant queries by combining authentication and authorization checks.
     * 
     * @param auth Spring Security authentication object
     * @return User object if authenticated and is admin
     * @throws UnauthorizedException if user is not authenticated
     * @throws ForbiddenException if user is authenticated but not an admin
     */
    private User getAuthenticatedAdminUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            logger.warn("Authentication attempt with null or invalid authentication object");
            throw new UnauthorizedException("User is not authenticated");
        }
        
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found for authenticated email: {}", email);
                    return new UnauthorizedException("Authenticated user not found in database");
                });
        
        // Check admin role with proper null safety
        boolean isAdmin = false;
        try {
            // First check the roles collection (new role system)
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                isAdmin = user.getRoles().stream()
                        .anyMatch(r -> r != null && "ROLE_ADMIN".equals(r.getName()));
            }
            // Fallback to legacy role field if roles collection is empty
            else if (user.getRole() != null) {
                isAdmin = "ADMIN".equalsIgnoreCase(user.getRole().trim());
            }
        } catch (Exception ex) {
            logger.error("Error checking admin role for user {}: {}", email, ex.getMessage(), ex);
            // In case of any error, deny access for security
            throw new ForbiddenException("Error validating user permissions");
        }
        
        if (!isAdmin) {
            logger.warn("Access denied: User {} attempted to access admin endpoint without admin privileges", email);
            throw new ForbiddenException("User does not have admin privileges");
        }
        
        logger.debug("Admin user {} successfully authenticated and authorized", email);
        return user;
    }

    /**
     * Get authenticated user without admin check (for create operation).
     * 
     * @param auth Spring Security authentication object
     * @return User object if authenticated
     * @throws UnauthorizedException if user is not authenticated
     */
    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            logger.warn("Authentication attempt with null or invalid authentication object");
            throw new UnauthorizedException("User is not authenticated");
        }
        
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found for authenticated email: {}", email);
                    return new UnauthorizedException("Authenticated user not found in database");
                });
        
        logger.debug("User {} successfully authenticated", email);
        return user;
    }

    @GetMapping
    public ResponseEntity<?> listAll(Authentication auth) {
        try {
            User admin = getAuthenticatedAdminUser(auth);
            logger.info("Admin {} requested list of all quizzes", admin.getEmail());
            
            List<Quiz> all = quizService.getAllQuizzes();
            List<QuizDto> dtos = all.stream().map(QuizMapper::toDto).collect(Collectors.toList());
            
            logger.debug("Returning {} quizzes", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized access attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (ForbiddenException ex) {
            logger.error("Forbidden access attempt: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "forbidden", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error listing quizzes: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @RequestBody CreateQuizRequest req) {
        try {
            User user = getAuthenticatedUser(auth);
            logger.info("User {} attempting to create quiz with title '{}'", user.getEmail(), req.getTitle());
            
            Quiz created = quizService.createFromRequest(req, user);
            
            logger.info("Successfully created quiz with ID {} for user {}", created.getId(), user.getEmail());
            return ResponseEntity.status(201).body(QuizMapper.toDto(created));
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized quiz creation attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (QuizAlreadyExistsException ex) {
            logger.error("Quiz creation failed - already exists: {}", ex.getMessage());
            return ResponseEntity.status(409).body(Map.of("error", "quiz_already_exists", "message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            logger.error("Quiz creation failed - invalid input: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_input", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error creating quiz: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "Failed to create quiz"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id, @RequestBody CreateQuizRequest req) {
        try {
            User admin = getAuthenticatedAdminUser(auth);
            logger.info("Admin {} attempting to update quiz with ID {}", admin.getEmail(), id);
            
            Quiz updated = quizService.updateFromRequest(id, req);
            
            logger.info("Successfully updated quiz with ID {} by admin {}", id, admin.getEmail());
            return ResponseEntity.ok(QuizMapper.toDto(updated));
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized quiz update attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (ForbiddenException ex) {
            logger.error("Forbidden quiz update attempt: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "forbidden", "message", ex.getMessage()));
        } catch (QuizNotFoundException ex) {
            logger.error("Quiz update failed - not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", "quiz_not_found", "message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            logger.error("Quiz update failed - invalid input: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_input", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error updating quiz with ID {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "Failed to update quiz"));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(Authentication auth, @PathVariable Long id) {
        try {
            User admin = getAuthenticatedAdminUser(auth);
            logger.info("Admin {} attempting to publish quiz with ID {}", admin.getEmail(), id);
            
            Quiz q = quizService.setPublished(id, true);
            
            logger.info("Successfully published quiz with ID {} by admin {}", id, admin.getEmail());
            return ResponseEntity.ok(QuizMapper.toDto(q));
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized quiz publish attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (ForbiddenException ex) {
            logger.error("Forbidden quiz publish attempt: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "forbidden", "message", ex.getMessage()));
        } catch (QuizNotFoundException ex) {
            logger.error("Quiz publish failed - not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", "quiz_not_found", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error publishing quiz with ID {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "Failed to publish quiz"));
        }
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<?> unpublish(Authentication auth, @PathVariable Long id) {
        try {
            User admin = getAuthenticatedAdminUser(auth);
            logger.info("Admin {} attempting to unpublish quiz with ID {}", admin.getEmail(), id);
            
            Quiz q = quizService.setPublished(id, false);
            
            logger.info("Successfully unpublished quiz with ID {} by admin {}", id, admin.getEmail());
            return ResponseEntity.ok(QuizMapper.toDto(q));
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized quiz unpublish attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (ForbiddenException ex) {
            logger.error("Forbidden quiz unpublish attempt: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "forbidden", "message", ex.getMessage()));
        } catch (QuizNotFoundException ex) {
            logger.error("Quiz unpublish failed - not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", "quiz_not_found", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error unpublishing quiz with ID {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "Failed to unpublish quiz"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        try {
            User admin = getAuthenticatedAdminUser(auth);
            logger.info("Admin {} attempting to delete quiz with ID {}", admin.getEmail(), id);
            
            quizService.deleteQuizById(id);
            
            logger.info("Successfully deleted quiz with ID {} by admin {}", id, admin.getEmail());
            return ResponseEntity.ok(Map.of("message", "Quiz deleted successfully", "id", id));
        } catch (UnauthorizedException ex) {
            logger.error("Unauthorized quiz deletion attempt: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
        } catch (ForbiddenException ex) {
            logger.error("Forbidden quiz deletion attempt: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "forbidden", "message", ex.getMessage()));
        } catch (QuizNotFoundException ex) {
            logger.error("Quiz deletion failed - not found: {}", ex.getMessage());
            return ResponseEntity.status(404).body(Map.of("error", "quiz_not_found", "message", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error deleting quiz with ID {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", "Failed to delete quiz"));
        }
    }
}