package org.nikolic.programm.utils;

import org.nikolic.programm.dtos.ChoiceDto;
import org.nikolic.programm.dtos.QuestionDto;
import org.nikolic.programm.dtos.QuizDto;
import org.nikolic.programm.entities.Choice;
import org.nikolic.programm.entities.Question;
import org.nikolic.programm.entities.Quiz;

import java.util.stream.Collectors;

public class QuizMapper {

    public static QuizDto toDto(Quiz q) {
        if (q == null) return null;
        QuizDto dto = new QuizDto();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setIsPublished(q.getIsPublished());
        dto.setCreatedByEmail(q.getCreatedBy() != null ? q.getCreatedBy().getEmail() : null);
        if (q.getQuestions() != null) {
            dto.setQuestions(q.getQuestions().stream().sorted((a,b)-> {
                Integer pa = a.getPosition()==null?0:a.getPosition();
                Integer pb = b.getPosition()==null?0:b.getPosition();
                return pa.compareTo(pb);
            }).map(QuizMapper::toQuestionDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static QuestionDto toQuestionDto(Question q) {
        if (q == null) return null;
        QuestionDto d = new QuestionDto();
        d.setId(q.getId());
        d.setQtype(q.getQtype() != null ? q.getQtype().name() : null);
        d.setText(q.getText());
        d.setAuxText(q.getAuxText());
        d.setPosition(q.getPosition());

        if (q.getChoices() != null) {
            d.setChoices(q.getChoices().stream()
                    .sorted((a,b)-> {
                        Integer pa = a.getPosition()==null?0:a.getPosition();
                        Integer pb = b.getPosition()==null?0:b.getPosition();
                        return pa.compareTo(pb);
                    })
                    .map(c -> {
                        ChoiceDto cd = new ChoiceDto();
                        cd.setId(c.getId());
                        cd.setText(c.getText());
                        return cd;
                    })
                    .collect(Collectors.toList()));
        }

        return d;
    }
}