package com.example.service;

import com.example.model.VacancyEntity;
import com.example.model.VacancyModel;
import com.example.repository.VacancyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VacancyParserHH implements VacancyParser {

    private final HhApiClient apiClient;
    private final VacancyMapper vacancyMapper;
    private final VacancyRepository vacancyRepository;

    public VacancyParserHH(HhApiClient apiClient,
                           VacancyMapper vacancyMapper,
                           VacancyRepository vacancyRepository) {
        this.apiClient = apiClient;
        this.vacancyMapper = vacancyMapper;
        this.vacancyRepository = vacancyRepository;
    }

    @Override
    public List<VacancyModel> fetchVacancies(String language) throws Exception {
        return fetchVacancies(language, null);
    }

    public List<VacancyModel> fetchVacancies(String language, String city) throws Exception {
        List<VacancyModel> result = new ArrayList<>();
        int page = 0;
        int maxPage = 100;
        int totalSaved = 0;
        int totalSkipped = 0;

        System.out.println("Starting to fetch vacancies for language: " + language + ", city: " + city);

        List<VacancyEntity> existingVacancies = vacancyRepository.findByLanguageAndCity(language, city);
        System.out.println("Found " + existingVacancies.size() + " existing vacancies in database");
        System.out.println("Search parameters - Language: '" + language + "', City: '" + city + "'");
        
        for (VacancyEntity entity : existingVacancies) {
            System.out.println("Adding existing vacancy: " + entity.getTitle() + 
                             " (Company: " + entity.getCompany() + 
                             ", City: " + entity.getCity() + ")");
            result.add(vacancyMapper.toModel(entity));
        }

        while (page < maxPage) {
            System.out.println("Fetching page " + page);
            JsonNode root = apiClient.fetchVacanciesPage(page, language, city);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) {
                System.out.println("No more items on page " + page);
                break;
            }

            for (JsonNode vacancyNode : items) {
                VacancyModel model = new VacancyModel(vacancyNode);
                VacancyEntity entity = vacancyMapper.toEntity(model);
                
                if (!vacancyRepository.existsByLink(entity.getLink())) {
                    try {
                        vacancyRepository.save(entity);
                        totalSaved++;
                        System.out.println("Saved new vacancy: " + entity.getTitle() + " in " + entity.getCity() + 
                                         " (Salary: " + entity.getSalary() + ")");
                        result.add(model);
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("Failed to save vacancy: " + entity.getLink() + " - " + e.getMessage());
                    }
                } else {
                    totalSkipped++;
                    System.out.println("Vacancy already exists: " + entity.getTitle() + " in " + entity.getCity());
                }
            }

            page++;
            if (page >= root.path("pages").asInt(0)) {
                System.out.println("Reached last page: " + page);
                break;
            }
        }

        System.out.println("Finished fetching vacancies. Total saved: " + totalSaved + 
                          ", Total skipped: " + totalSkipped + 
                          ", Total from DB: " + existingVacancies.size() + 
                          ", Total in result: " + result.size());

        return result;
    }
}
