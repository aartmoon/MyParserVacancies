package com.example.service.general;

import com.example.model.Vacancy;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VacancyLogger {
    public void logStartFetching(String language, String city) {
        log.info("Starting to fetch vacancies for language: {} and city: {}", language, city);
    }

    public void logExistingVacancies(int count) {
        log.info("Found {} existing vacancies in database", count);
    }

    public void logSearchParameters(String language, String city) {
        log.info("Search parameters - Language: {}, City: {}", language, city);
    }

    public void logExistingVacancy(Vacancy vacancy) {
        log.info("Adding existing vacancy: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logFetchingPage(int page) {
        log.info("Fetching page {}", page);
    }

    public void logNoMoreItems(int page) {
        log.info("No more items on page {}", page);
    }

    public void logSavedVacancy(Vacancy vacancy) {
        log.info("Saved new vacancy: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logFailedToSave(String link, String error) {
        log.error("Failed to save vacancy with link {}: {}", link, error);
    }

    public void logExistingVacancy(Vacancy vacancy, boolean isExisting) {
        log.info("Vacancy already exists: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logLastPage(int page) {
        log.info("Reached last page: {}", page);
    }

    public void logSummary(int totalSaved, int totalSkipped, int totalFromDb, int totalInResult) {
        log.info("Summary - Saved: {}, Skipped: {}, From DB: {}, Total in result: {}", 
            totalSaved, totalSkipped, totalFromDb, totalInResult);
    }

    public void logDateParseError(String dateStr, String error) {
        log.error("Failed to parse date: {}. Error: {}", dateStr, error);
    }

    public void logDateFormatError(java.time.LocalDateTime date, String error) {
        log.error("Failed to format date: {}. Error: {}", date, error);
    }
    public void logDateParseError(String dateStr, Exception e) {
        log.error("Failed to parse publishedAt date: {}. Error: {}", dateStr, e.getMessage());
    }
} 