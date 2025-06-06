package com.example.service.general;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.example.config.Constants.*;

@Service
@RequiredArgsConstructor
public class VacancyScheduler {
    private final VacancyService vacancyService;
    private final VacancyLogger logger;

    @Scheduled(fixedRate = SCHEDULE_RATE)
    public void scheduledRefresh() {
        try {
            logger.logStartFetching("scheduled", "all");
            for (String c : CITIES) {
                for (String lang : LANGUAGES) {
                    vacancyService.refreshVacancies(lang, c);
                }
            }
        } catch (Exception e) {
            logger.logFailedToSave("scheduled_refresh", e.getMessage());
        }
    }
}