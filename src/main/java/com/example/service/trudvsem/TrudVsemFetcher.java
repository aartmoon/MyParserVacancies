package com.example.service.trudvsem;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrudVsemFetcher {

    private static final String API_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";
    private static final int MAX_PAGES = 10;
    private static final int PAGE_SIZE = 30;

    private final RestTemplate restTemplate;
    private final VacancyRepository vacancyRepository;
    private final VacancyLogger logger;

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

        while (page < MAX_PAGES) {
            try {
                logger.logFetchingPage(page);

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                        .queryParam("offset", page * PAGE_SIZE)
                        .queryParam("limit", PAGE_SIZE);

                if (language != null && !language.isBlank()) {
                    builder.queryParam("query", language);
                }
                if (city != null && !city.isBlank()) {
                    // Используем код региона напрямую из маппинга
                    Map<String, String> cityToRegionCode = Map.of(
                            "Москва", "77",
                            "Санкт-Петербург", "78",
                            "Новосибирск", "54",
                            "Екатеринбург", "66",
                            "Казань", "16",
                            "Нижний Новгород", "52"
                    );

                    String regionCode = cityToRegionCode.get(city);
                    if (regionCode != null) {
                        builder.queryParam("region_code", regionCode);
                    }
                }

                URI uri = builder.build().encode().toUri();
                log.info("Requesting URL: {}", uri.toString());

                String jsonResponse = restTemplate.getForObject(uri, String.class);

                if (jsonResponse == null || jsonResponse.isBlank()) {
                    logger.logNoMoreItems(page);
                    break;
                }

                JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
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

                // Проверяем общее количество вакансий
                JsonObject meta = root.getAsJsonObject("meta");
                if (meta != null && meta.has("total")) {
                    int total = meta.get("total").getAsInt();
                    if (page * PAGE_SIZE >= total) {
                        logger.logNoMoreItems(page);
                        break;
                    }
                }

                for (JsonElement element : vacArray) {
                    try {
                        Vacancy vacancy = parseVacancy(element.getAsJsonObject());
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
                        log.warn("Пропуск вакансии из-за ошибки парсинга: {}", ex.getMessage());
                    }
                }
                page++;
            } catch (Exception e) {
                log.error("Ошибка при запросе вакансий с Trudvsem: {}", e.getMessage(), e);
                break;
            }
        }

        logger.logSummary(totalSaved, totalSkipped, existingVacancies.size(), result.size());
        return result;
    }

    private Vacancy parseVacancy(JsonObject wrapper) {
        try {
            // Получаем объект vacancy из wrapper
            JsonObject vacObj = wrapper.has("vacancy")
                    ? wrapper.getAsJsonObject("vacancy")
                    : wrapper;

            if (!vacObj.has("job-name") || !vacObj.has("vac_url")) {
                return null;
            }

            String link = vacObj.get("vac_url").getAsString();
            if (vacancyRepository.existsByLink(link)) {
                return null;
            }

            Vacancy vac = new Vacancy();
            vac.setTitle(vacObj.get("job-name").getAsString());
            vac.setLink(link);

            // Парсинг компании
            if (vacObj.has("company") && vacObj.get("company").isJsonObject()) {
                JsonObject comp = vacObj.getAsJsonObject("company");
                if (comp.has("name")) {
                    vac.setCompany(comp.get("name").getAsString());
                }
            }

            // Парсинг города из маппинга
            if (vacObj.has("region") && vacObj.get("region").isJsonObject()) {
                JsonObject region = vacObj.getAsJsonObject("region");
                if (region.has("name")) {
                    String regionName = region.get("name").getAsString();
                    // Извлекаем название города из полного названия региона
                    String city = extractCityFromRegion(regionName);
                    if (city != null) {
                        vac.setCity(city);
                    }
                }
            }

            // Парсинг зарплаты
            if (vacObj.has("salary_min") || vacObj.has("salary_max")) {
                Integer salaryFrom = null;
                Integer salaryTo = null;

                if (vacObj.has("salary_min")) {
                    int minSalary = vacObj.get("salary_min").getAsInt();
                    if (minSalary > 0) {
                        salaryFrom = minSalary;
                    }
                }
                if (vacObj.has("salary_max")) {
                    int maxSalary = vacObj.get("salary_max").getAsInt();
                    if (maxSalary > 0) {
                        salaryTo = maxSalary;
                    }
                }

                // Устанавливаем зарплату только если хотя бы одно значение не null
                if (salaryFrom != null || salaryTo != null) {
                    vac.setSalaryFrom(salaryFrom);
                    vac.setSalaryTo(salaryTo);
                    vac.setCurrency("RUB");
                }
            }

            // Парсинг обязанностей
            if (vacObj.has("duty")) {
                vac.setResponsibility(vacObj.get("duty").getAsString());
            }

            // Парсинг требований
            if (vacObj.has("requirement") && vacObj.get("requirement").isJsonObject()) {
                JsonObject req = vacObj.getAsJsonObject("requirement");
                StringBuilder requirements = new StringBuilder();

                if (req.has("education")) {
                    requirements.append("Образование: ").append(req.get("education").getAsString()).append("\n");
                }
                if (req.has("experience")) {
                    requirements.append("Опыт работы: ").append(req.get("experience").getAsInt()).append(" лет\n");
                }

                vac.setRequirement(requirements.toString().trim());
            }

            // Парсинг даты публикации
            if (vacObj.has("creation-date")) {
                String dateStr = vacObj.get("creation-date").getAsString();
                try {
                    LocalDate date = LocalDate.parse(dateStr);
                    vac.setPublishedAt(date.atStartOfDay());
                } catch (Exception ex) {
                    log.warn("Не удалось распарсить дату [{}]: {}", dateStr, ex.getMessage());
                }
            }

            return vac;
        } catch (Exception e) {
            log.error("Ошибка при парсинге вакансии: {}", e.getMessage());
            return null;
        }
    }

    // Добавляем метод для извлечения города из названия региона
    private String extractCityFromRegion(String regionName) {
        // Убираем "Город" из начала названия
        String city = regionName.replaceFirst("^Город\\s+", "");

        // Проверяем, есть ли город в нашем маппинге
        Map<String, String> cityToRegionCode = Map.of(
                "Москва", "77",
                "Санкт-Петербург", "78",
                "Новосибирск", "54",
                "Екатеринбург", "66",
                "Казань", "16",
                "Нижний Новгород", "52"
        );

        // Ищем город в маппинге
        for (String knownCity : cityToRegionCode.keySet()) {
            if (city.contains(knownCity)) {
                return knownCity;
            }
        }

        return null;
    }
}