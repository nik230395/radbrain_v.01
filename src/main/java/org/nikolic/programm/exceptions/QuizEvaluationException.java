package org.nikolic.programm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when quiz evaluation fails
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class QuizEvaluationException extends DataOperationException {
    public QuizEvaluationException(String message) {
        super("Fehler bei Quiz-Auswertung: " + message);
    }

    public QuizEvaluationException(String message, Throwable cause) {
        super("Fehler bei Quiz-Auswertung: " + message, cause);
    }
}
