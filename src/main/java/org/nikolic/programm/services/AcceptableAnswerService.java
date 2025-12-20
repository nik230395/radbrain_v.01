package org.nikolic.programm.services;

import org.nikolic.programm.entities.AcceptableAnswer;
import org.nikolic.programm.entities.MatchMode;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.repositories.AcceptableAnswerRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service für akzeptierte Freitext-Antworten
@Service
@Transactional
public class AcceptableAnswerService {
    @Autowired
    private AcceptableAnswerRepository acceptableAnswerRepository;
    @Autowired
    private QuestionRepository questionRepository;

    // Neue akzeptierte Antwort speichern
    public AcceptableAnswer createAcceptableAnswer(Long questionId, String answerText, String matchMode) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Frage nicht gefunden"));
        AcceptableAnswer answer = new AcceptableAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(answerText);

        // robustes Mapping des matchMode-Strings
        if (matchMode == null) {
            throw new RuntimeException("matchMode erforderlich");
        }
        String normalized = matchMode.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
        answer.setMatchMode(MatchMode.valueOf(normalized));

        return acceptableAnswerRepository.save(answer);
    }

    // Alle akzeptierten Antworten für eine Frage
    public List<AcceptableAnswer> getAnswersByQuestion(Long questionId) {
        return acceptableAnswerRepository.findByQuestionId(questionId);
    }
}