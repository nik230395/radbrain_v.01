package org.nikolic.programm. services;

import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm. repositories.ChoiceRepository;
import org.nikolic.programm.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation. Autowired;
import org. springframework.stereotype.Service;
import org.springframework.transaction.annotation. Transactional;

import java.util.List;

@Service
@Transactional
public class ChoiceService {
    @Autowired
    private ChoiceRepository choiceRepository;
    @Autowired
    private QuestionRepository questionRepository;

    // Create new choice
    public Choice createChoice(Long questionId, String text, Boolean isCorrect) {
        Question question = questionRepository. findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Integer maxPosition = choiceRepository.findMaxPositionByQuestionId(questionId);
        if (maxPosition == null) maxPosition = 0;

        Choice choice = new Choice();
        choice.setQuestion(question);
        choice.setText(text);
        choice.setIs_correct(isCorrect);
        choice.setPosition(maxPosition + 1);

        return choiceRepository.save(choice);
    }

    // Get choices for a question
    public List<Choice> getChoicesByQuestion(Long questionId) {
        return choiceRepository.findByQuestionIdOrderByPositionAsc(questionId);
    }

    // Delete choice
    public void deleteChoice(Long id) {
        choiceRepository.deleteById(id);
    }

    // Update choice
    public Choice updateChoice(Long id, String text, Boolean isCorrect) {
        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Choice not found"));

        choice.setText(text);
        choice.setIs_correct(isCorrect);

        return choiceRepository.save(choice);
    }

    // Get correct choices for a question
    public List<Choice> getCorrectChoicesByQuestion(Long questionId) {
        return choiceRepository.findByQuestionIdAndIsCorrectOrderByPositionAsc(questionId, true);
    }

    // Check if question has correct answers
    public boolean questionHasCorrectAnswers(Long questionId) {
        return choiceRepository. countByQuestionIdAndIsCorrect(questionId, true) > 0;
    }

    // ✅ Fixed: Reorder choices for a question
    public void reorderChoices(Long questionId, List<Long> choiceIds) {
        // Use traditional for loop instead of lambda
        for (int i = 0; i < choiceIds. size(); i++) {
            final int position = i + 1; // Make it effectively final
            Long choiceId = choiceIds. get(i);

            choiceRepository.findById(choiceId).ifPresent(choice -> {
                choice.setPosition(position); // Now position is effectively final
                choiceRepository.save(choice);
            });
        }
    }

    // Alternative approach without lambda
    public void reorderChoicesAlternative(Long questionId, List<Long> choiceIds) {
        for (int i = 0; i < choiceIds. size(); i++) {
            Long choiceId = choiceIds. get(i);
            Choice choice = choiceRepository.findById(choiceId).orElse(null);
            if (choice != null) {
                choice.setPosition(i + 1);
                choiceRepository.save(choice);
            }
        }
    }
}