package org.nikolic.programm.services;

import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.repositories.ChoiceRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Service für Antwortoptionen (Choices)
@Service
@Transactional
public class ChoiceService {
    @Autowired
    private ChoiceRepository choiceRepository;
    @Autowired
    private QuestionRepository questionRepository;

    // Neue Antwortoption erzeugen
    public Choice createChoice(Long questionId, String text, Boolean isCorrect) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Frage nicht gefunden"));
        Integer maxPosition = choiceRepository.findMaxPositionByQuestionId(questionId);
        if (maxPosition == null) maxPosition = 0;

        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText(text);
        choice.setIsCorrect(isCorrect);
        choice.setPosition(maxPosition + 1);

        return choiceRepository.save(choice);
    }

    // Optionen für eine Frage
    public List<Choice> getChoicesByQuestion(Long questionId) {
        return choiceRepository.findByQuestionIdOrderByPositionAsc(questionId);
    }

    // Antwortoption löschen
    public void deleteChoice(Long id) {
        choiceRepository.deleteById(id);
    }
}