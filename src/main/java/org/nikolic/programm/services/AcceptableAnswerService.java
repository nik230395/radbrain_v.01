package org.nikolic.programm.services;

import org. nikolic.programm.entities. AcceptableAnswer;
import org.nikolic.programm.entities. MatchMode;
import org.nikolic. programm.entities.Question;
import org.nikolic. programm.repositories.AcceptableAnswerRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AcceptableAnswerService {
    @Autowired
    private AcceptableAnswerRepository acceptableAnswerRepository;
    @Autowired
    private QuestionRepository questionRepository;

    // Create new acceptable answer
    public AcceptableAnswer createAcceptableAnswer(Long questionId, String answerText, String matchMode) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        AcceptableAnswer answer = new AcceptableAnswer();
        answer.setQuestion(question);
        answer.setAnswer_text(answerText); // ✅ Fixed: Use setAnswer_text() instead of setAnswerText()

        // Robust mapping of matchMode string
        if (matchMode == null) {
            throw new RuntimeException("matchMode required");
        }
        String normalized = matchMode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
        answer.setMatch_mode(MatchMode.valueOf(normalized)); // ✅ Fixed: Use setMatch_mode()

        return acceptableAnswerRepository. save(answer);
    }

    // Get all acceptable answers for a question
    public List<AcceptableAnswer> getAnswersByQuestion(Long questionId) {
        return acceptableAnswerRepository.findByQuestionId(questionId);
    }

    // Update acceptable answer
    public AcceptableAnswer updateAcceptableAnswer(Long id, String answerText, String matchMode) {
        AcceptableAnswer answer = acceptableAnswerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acceptable answer not found"));

        answer.setAnswer_text(answerText); // ✅ Fixed: Use setAnswer_text()

        if (matchMode != null) {
            String normalized = matchMode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
            answer.setMatch_mode(MatchMode.valueOf(normalized)); // ✅ Fixed: Use setMatch_mode()
        }

        return acceptableAnswerRepository. save(answer);
    }

    // Delete acceptable answer
    public void deleteAcceptableAnswer(Long id) {
        acceptableAnswerRepository.deleteById(id);
    }

    // Get acceptable answers by match mode
    public List<AcceptableAnswer> getAnswersByQuestionAndMatchMode(Long questionId, MatchMode matchMode) {
        return acceptableAnswerRepository.findByQuestionIdAndMatchMode(questionId, matchMode);
    }

    // Check if answer text matches any acceptable answer for a question
    public boolean isAnswerAcceptable(Long questionId, String userAnswer) {
        List<AcceptableAnswer> acceptableAnswers = getAnswersByQuestion(questionId);

        for (AcceptableAnswer acceptable : acceptableAnswers) {
            if (matchesAcceptableAnswer(userAnswer, acceptable)) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if user answer matches acceptable answer
    private boolean matchesAcceptableAnswer(String userAnswer, AcceptableAnswer acceptable) {
        String acceptableText = acceptable.getAnswer_text(); // ✅ Fixed: Use getAnswer_text()
        if (acceptableText == null) return false;

        switch (acceptable. getMatch_mode()) { // ✅ Fixed: Use getMatch_mode()
            case EXACT:
                return userAnswer.equals(acceptableText);

            case CASE_INSENSITIVE:
                return userAnswer.equalsIgnoreCase(acceptableText);

            case CONTAINS:
                return userAnswer.toLowerCase().contains(acceptableText.toLowerCase());

            case REGEX:
                try {
                    return userAnswer.matches(acceptableText);
                } catch (Exception e) {
                    return false; // Invalid regex
                }

            default:
                return false;
        }
    }

    // Bulk create acceptable answers for a question
    public List<AcceptableAnswer> createMultipleAcceptableAnswers(Long questionId, List<String> answerTexts, MatchMode matchMode) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        List<AcceptableAnswer> answers = answerTexts.stream()
                .map(text -> {
                    AcceptableAnswer answer = new AcceptableAnswer();
                    answer.setQuestion(question);
                    answer.setAnswer_text(text); // ✅ Fixed: Use setAnswer_text()
                    answer.setMatch_mode(matchMode); // ✅ Fixed: Use setMatch_mode()
                    return answer;
                })
                .toList();

        return acceptableAnswerRepository.saveAll(answers);
    }
}