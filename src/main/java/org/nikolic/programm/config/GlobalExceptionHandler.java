package org.nikolic.programm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * GlobalExceptionHandler
 *
 * Zentrales Exception Handling für alle Controller
 * Verhindert Stack Traces in API-Responses
 */
@RestControllerAdvice(basePackages = "org.nikolic.programm.controllers")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Validation Errors (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Validierung fehlgeschlagen",
                        errors,
                        request
                ));
    }

    /**
     * Illegal Argument (z.B. ungültige Parameter)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        null,
                        request
                ));
    }

    /**
     * Not Found (z.B. Quiz/User nicht gefunden)
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoSuchElementException ex, WebRequest request) {

        logger.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        null,
                        request
                ));
    }

    /**
     * Illegal State (z.B. Quiz kann nicht veröffentlicht werden)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {

        logger.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(createErrorResponse(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        null,
                        request
                ));
    }

    /**
     * Access Denied (Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        logger.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(
                        HttpStatus.FORBIDDEN,
                        "Zugriff verweigert - Keine Berechtigung",
                        null,
                        request
                ));
    }

    /**
     * Bad Credentials (Login fehlgeschlagen)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        logger.warn("Bad credentials: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Ungültige Anmeldedaten",
                        null,
                        request
                ));
    }

    /**
     * Runtime Exception (Allgemeine Fehler)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        logger.error("Runtime exception: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ein Fehler ist aufgetreten: " + ex.getMessage(),
                        null,
                        request
                ));
    }

    /**
     * Generic Exception (Catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ein unerwarteter Fehler ist aufgetreten",
                        null,
                        request
                ));
    }

    /**
     * Erstellt standardisierte Error Response
     * ✅ FIX: Verwendet System.currentTimeMillis() statt LocalDateTime
     */
    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String message,
            Object details,
            WebRequest request) {

        Map<String, Object> response = new HashMap<>();


        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);


        String path = request.getDescription(false);
        if (path != null) {
            path = path.replace("uri=", "");
        }
        response.put("path", path);

        if (details != null) {
            response.put("details", details);
        }

        return response;
    }
}