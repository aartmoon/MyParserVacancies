package com.example.service.general;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.config.Constants;
import com.example.service.trudvsem.TrudVsemFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class VacancyService {
    private final List<VacancyFetcher> vacancyFetchers;
    private final VacancyRepository vacancyRepository;
    private final VacancyFilter vacancyFilter;
    private final VacancyCleaner vacancyCleaner;

    public void refreshVacancies(String language, String city) throws Exception {
        if (language != null && !language.isEmpty()) {
            for (VacancyFetcher fetcher : vacancyFetchers) {
                fetcher.fetchVacancies(language, city);
            }
        } else {
            for (String lang : Constants.LANGUAGES) {
                for (VacancyFetcher fetcher : vacancyFetchers) {
                    fetcher.fetchVacancies(lang, city);
                }
            }
        }
    }

    public List<Vacancy> getVacancies(String language, String city, boolean withSalary) {
        List<Vacancy> vacancies = new ArrayList<>();
        
        if (language != null && !language.isEmpty()) {
            if (city != null && !city.isEmpty()) {
                vacancies.addAll(vacancyRepository.findByLanguageAndCity(language, city));
            } else {
                for (String c : Constants.CITIES) {
                    vacancies.addAll(vacancyRepository.findByLanguageAndCity(language, c));
                }
            }
        } else if (city != null && !city.isEmpty()) {
            for (String lang : Constants.LANGUAGES) {
                vacancies.addAll(vacancyRepository.findByLanguageAndCity(lang, city));
            }
        } else {
            vacancies.addAll(vacancyRepository.findAll());
        }

        // чистим вакансии от <тегов>
        vacancies = vacancyCleaner.clean(vacancies);

        if (withSalary) {
            return vacancyFilter.filterBySalary(vacancies);
        }

        return vacancies;
    }
}