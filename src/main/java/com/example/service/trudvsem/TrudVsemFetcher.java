package com.example.service.trudvsem;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyFetcher;
import com.example.service.general.VacancyLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import static com.example.config.Constants.MAX_PAGES_TRUDVSEM;

@Service
@RequiredArgsConstructor
public class TrudVsemFetcher implements VacancyFetcher {

    private final TrudVsemApi api;
    private final TrudVsemToVacancy parser;
    private final VacancyRepository vacancyRepository;
    private final VacancyLogger logger;

    @Override
    public List<Vacancy> fetchVacancies(String language, String city) {
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

        while (page < MAX_PAGES_TRUDVSEM) {
            try {
                logger.logFetchingPage(page);

                JsonObject root = api.fetchVacanciesPage(language, city, page);
                if (root == null) {
                    logger.logNoMoreItems(page);
                    break;
                }

                JsonObject results = root.getAsJsonObject("results");
                if (results == null) {
                    logger.logNoMoreItems(page);
                    break;
                }

                JsonArray vacArray = results.getAsJsonArray("vacancies");
                if (vacArray == null || vacArray.isEmpty()) {
                    logger.logNoMoreItems(page);
                    break;
                }

                if (shouldStopFetching(root, page)) {
                    logger.logNoMoreItems(page);
                    break;
                }

                processVacancies(vacArray, language, result, totalSaved, totalSkipped);
                page++;
            } catch (Exception e) {
                logger.logFailedToSave("unknown", e.getMessage());
                break;
            }
        }

        logger.logSummary(totalSaved, totalSkipped, existingVacancies.size(), result.size());
        return result;
    }

    private boolean shouldStopFetching(JsonObject root, int page) {
        JsonObject meta = root.getAsJsonObject("meta");
        if (meta != null && meta.has("total")) {
            int total = meta.get("total").getAsInt();
            return page * 30 >= total;
        }
        return false;
    }

    private void processVacancies(JsonArray vacArray, String language, List<Vacancy> result, int totalSaved, int totalSkipped) {
        for (JsonElement element : vacArray) {
            try {
                Vacancy vacancy = parser.parseVacancy(element.getAsJsonObject());
                if (vacancy != null) {
                    vacancy.setLanguage(language);

                    if (!vacancyRepository.existsByLink(vacancy.getLink())) {
                        try {
                            vacancyRepository.save(vacancy);
                            totalSaved++;
                            logger.logSavedVacancy(vacancy);
                            result.add(vacancy);
                        } catch (Exception e) {
                            logger.logFailedToSave(vacancy.getLink(), e.getMessage());
                        }
                    } else {
                        totalSkipped++;
                        logger.logExistingVacancy(vacancy, true);
                    }
                }
            } catch (Exception ex) {
                logger.logFailedToSave("parsing_error", ex.getMessage());
            }
        }
    }
}