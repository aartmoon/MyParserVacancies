package com.example.service.general;

import com.example.model.Vacancy;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VacancyCleaner {

    public Vacancy clean(Vacancy vacancy) {
        if (vacancy.getRequirement() != null) {
            vacancy.setRequirement(cleanText(vacancy.getRequirement()));
        }
        if (vacancy.getResponsibility() != null) {
            vacancy.setResponsibility(cleanText(vacancy.getResponsibility()));
        }
        return vacancy;
    }

    public List<Vacancy> clean(List<Vacancy> vacancies) {
        return vacancies.stream()
                .map(this::clean)
                .collect(Collectors.toList());
    }

    private String cleanText(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
