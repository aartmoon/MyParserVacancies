package com.example.service.general;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.config.Constants;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class VacancyService {
    private final VacancyParser vacancyParser;
    private final VacancyRepository vacancyRepository;
    private final VacancyFilter vacancyFilter;

    public VacancyService(
            VacancyParser vacancyParser, 
            VacancyRepository vacancyRepository,
            VacancyFilter vacancyFilter) {
        this.vacancyParser = vacancyParser;
        this.vacancyRepository = vacancyRepository;
        this.vacancyFilter = vacancyFilter;
    }

    public void refreshVacancies(String language, String city) throws Exception {
        if (language != null && !language.isEmpty()) {
            vacancyParser.fetchVacancies(language, city);
        } else {
            for (String lang : Constants.LANGUAGES) {
                vacancyParser.fetchVacancies(lang, city);
            }
        }
    }

    public List<Vacancy> getVacancies(String language, String city, boolean withSalary) {
        List<Vacancy> vacancies = new ArrayList<>();
        
        if (language != null && !language.isEmpty()) {
            if (city != null && !city.isEmpty()) {
                // Поиск по языку и городу
                vacancies.addAll(vacancyRepository.findByLanguageAndCity(language, city));
            } else {
                // Поиск по языку во всех городах
                for (String c : Constants.CITIES) {
                    vacancies.addAll(vacancyRepository.findByLanguageAndCity(language, c));
                }
            }
        } else if (city != null && !city.isEmpty()) {
            // Поиск по городу для всех языков
            for (String lang : Constants.LANGUAGES) {
                vacancies.addAll(vacancyRepository.findByLanguageAndCity(lang, city));
            }
        } else {
            // Если ничего не выбрано - показываем все вакансии
            vacancies.addAll(vacancyRepository.findAll());
        }

        if (withSalary) {
            return vacancyFilter.filterBySalary(vacancies);
        }
        return vacancies;
    }
}