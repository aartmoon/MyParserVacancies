package com.example.service.general;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.config.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacancyService {
    private final List<VacancyFetcher> vacancyFetchers;
    private final VacancyRepository vacancyRepository;
    private final VacancyFilter vacancyFilter;
    private final VacancyCleaner vacancyCleaner;

    private static final int FIXED_LIST_SIZE = 50;

    public void refreshVacancies(String language, String city) throws Exception {
        List<String> languagesToFetch = resolveListOrAll(
                language,
                Constants.LANGUAGES
        );
        List<String> citiesToFetch = resolveListOrAll(
                city,
                Constants.CITIES
        );

        for (String lang : languagesToFetch) {
            for (String c : citiesToFetch) {
                for (VacancyFetcher fetcher : vacancyFetchers) {
                    fetcher.fetchVacancies(lang, c);
                }
            }
        }
    }

    public List<Vacancy> getVacancies(String language, String city, boolean withSalary) {
        List<String> languagesToQuery = resolveListOrAll(
                language,
                Constants.LANGUAGES
        );
        List<String> citiesToQuery = resolveListOrAll(
                city,
                Constants.CITIES
        );

        List<Vacancy> vacancies = new ArrayList<>();

        if (languagesToQuery.isEmpty() && citiesToQuery.isEmpty()) {
            vacancies = vacancyRepository.findAll();
        } else {
            for (String lang : languagesToQuery) {
                for (String c : citiesToQuery) {
                    vacancies.addAll(vacancyRepository.findByLanguageAndCity(lang, c));
                }
            }
        }

        vacancies = vacancyCleaner.clean(vacancies);

        List<Vacancy> filtered = withSalary
                ? vacancyFilter.filterBySalary(vacancies)
                : vacancies;

        if (filtered.size() > FIXED_LIST_SIZE) {
            return new ArrayList<>(filtered.subList(0, FIXED_LIST_SIZE));
        } else {
            return filtered;
        }
    }

    private List<String> resolveListOrAll(String value, List<String> allValues) {
        if (value != null && !value.isBlank()) {
            return Collections.singletonList(value);
        }
        if (allValues != null && !allValues.isEmpty()) {
            return new ArrayList<>(allValues);
        }
        return Collections.emptyList();
    }

    public List<Vacancy> getAllVacanciesForStats() {
        List<Vacancy> vacancies = vacancyRepository.findAll();
        return vacancyCleaner.clean(vacancies);
    }
}
