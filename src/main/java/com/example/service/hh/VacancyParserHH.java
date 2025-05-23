package com.example.service.hh;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyLogger;
import com.example.service.general.VacancyParser;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.example.config.Constants;
import java.util.ArrayList;
import java.util.List;

@Service
public class VacancyParserHH implements VacancyParser {

    private final HhApiClient apiClient;
    private final VacancyRepository vacancyRepository;
    private final VacancyLogger logger;

    public VacancyParserHH(HhApiClient apiClient,
                           VacancyRepository vacancyRepository,
                           VacancyLogger logger) {
        this.apiClient = apiClient;
        this.vacancyRepository = vacancyRepository;
        this.logger = logger;
    }

    @Override
    public List<Vacancy> fetchVacancies(String language, String city) throws Exception {
        List<Vacancy> result = new ArrayList<>();
        int page = 0;
        int totalSaved = 0;
        int totalSkipped = 0;

        logger.logStartFetching(language, city);

        List<Vacancy> existingVacancies = vacancyRepository.findByLanguageAndCity(language, city);
        logger.logExistingVacancies(existingVacancies.size());
        logger.logSearchParameters(language, city);

        for (Vacancy vacancy : existingVacancies) {
            logger.logExistingVacancy(vacancy);
            result.add(vacancy);
        }

        while (page < Constants.MAX_PAGE) {
            logger.logFetchingPage(page);

            JsonNode root = apiClient.fetchVacanciesPage(page, language, city);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) {
                logger.logNoMoreItems(page);
                break;
            }

            for (JsonNode vacancyNode : items) {
                Vacancy vacancy = Vacancy.fromJson(vacancyNode);
                vacancy.setLanguage(language);

                if (!vacancyRepository.existsByLink(vacancy.getLink())) {
                    try {
                        vacancyRepository.save(vacancy);
                        totalSaved++;
                        logger.logSavedVacancy(vacancy);
                        result.add(vacancy);
                    } catch (DataIntegrityViolationException e) {
                        logger.logFailedToSave(vacancy.getLink(), e.getMessage());
                    }
                } else {
                    totalSkipped++;
                    logger.logExistingVacancy(vacancy, true);
                }
            }
            page++;
        }

        logger.logSummary(totalSaved, totalSkipped, existingVacancies.size(), result.size());
        return result;
    }
}
