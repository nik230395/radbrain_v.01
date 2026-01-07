package org.nikolic.programm.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitQuizRequest {

    @NotNull(message = "Quiz-ID ist erforderlich")
    private Long quizId;

    @NotNull(message = "Antworten sind erforderlich")
    private List<AnswerSubmission> answers;

    // Constructors
    public SubmitQuizRequest() {}

    public SubmitQuizRequest(Long quizId, List<AnswerSubmission> answers) {
        this.quizId = quizId;
        this.answers = answers;
    }

    // Getters and Setters
    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public List<AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    // Inner class for individual answers
    public static class AnswerSubmission {
        @NotNull(message = "Fragen-ID ist erforderlich")
        private Long questionId;

        @NotNull(message = "Auswahl-ID ist erforderlich")
        private Long choiceId;

        // Constructors
        public AnswerSubmission() {}

        public AnswerSubmission(Long questionId, Long choiceId) {
            this.questionId = questionId;
            this.choiceId = choiceId;
        }

        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Long getChoiceId() {
            return choiceId;
        }

        public void setChoiceId(Long choiceId) {
            this.choiceId = choiceId;
        }
    }

    @Override
    public String toString() {
        return "SubmitQuizRequest{" +
                "quizId=" + quizId +
                ", answers=" + answers +
                '}';
    }
}