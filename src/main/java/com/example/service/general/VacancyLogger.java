package com.example.service.general;

import com.example.model.Vacancy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VacancyLogger {
    private static final Logger logger = LoggerFactory.getLogger(VacancyLogger.class);

    public void logStartFetching(String language, String city) {
        logger.info("Starting to fetch vacancies for language: {} and city: {}", language, city);
    }

    public void logExistingVacancies(int count) {
        logger.info("Found {} existing vacancies in database", count);
    }

    public void logSearchParameters(String language, String city) {
        logger.info("Search parameters - Language: {}, City: {}", language, city);
    }

    public void logExistingVacancy(Vacancy vacancy) {
        logger.info("Adding existing vacancy: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logFetchingPage(int page) {
        logger.info("Fetching page {}", page);
    }

    public void logNoMoreItems(int page) {
        logger.info("No more items on page {}", page);
    }

    public void logSavedVacancy(Vacancy vacancy) {
        logger.info("Saved new vacancy: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logFailedToSave(String link, String error) {
        logger.error("Failed to save vacancy with link {}: {}", link, error);
    }

    public void logExistingVacancy(Vacancy vacancy, boolean isExisting) {
        logger.info("Vacancy already exists: {} at {}", vacancy.getTitle(), vacancy.getCompany());
    }

    public void logLastPage(int page) {
        logger.info("Reached last page: {}", page);
    }

    public void logSummary(int totalSaved, int totalSkipped, int totalFromDb, int totalInResult) {
        logger.info("Summary - Saved: {}, Skipped: {}, From DB: {}, Total in result: {}", 
            totalSaved, totalSkipped, totalFromDb, totalInResult);
    }

    public void logDateParseError(String dateStr, String error) {
        logger.error("Failed to parse date: {}. Error: {}", dateStr, error);
    }

    public void logDateFormatError(java.time.LocalDateTime date, String error) {
        logger.error("Failed to format date: {}. Error: {}", date, error);
    }
} 