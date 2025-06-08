package com.example.service.general;

import com.example.model.Vacancy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacancyStatsService {

    private final VacancyService vacancyService;

    public Map<String, Double> getAverageSalaryByLanguage() {
        List<Vacancy> vacancies = vacancyService.getAllVacanciesForStats();

        return vacancies.stream()
                .filter(v -> v.getSalaryFrom() != null || v.getSalaryTo() != null)
                .collect(Collectors.groupingBy(
                        Vacancy::getLanguage,
                        Collectors.averagingDouble(v -> {
                            double sum = 0;
                            int count = 0;
                            if (v.getSalaryFrom() != null) {
                                sum += v.getSalaryFrom();
                                count++;
                            }
                            if (v.getSalaryTo() != null) {
                                sum += v.getSalaryTo();
                                count++;
                            }
                            return count > 0 ? sum / count : 0;
                        })
                ));
    }

    public double getTotalAverageSalary(Map<String, Double> averageByLang) {
        return averageByLang.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
