package com.example.controller;

import com.example.model.Vacancy;
import com.example.service.general.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vacancy-stats")
@RequiredArgsConstructor
public class VacancyStatsController {

    private final VacancyService vacancyService;

    @GetMapping
    public String getStats(Model model) {

        List<Vacancy> vacancies = vacancyService.getVacancies(null, null, true);

        Map<String, Double> averageSalaryByLanguage = vacancies.stream()
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

        double totalAverageSalary = averageSalaryByLanguage.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        model.addAttribute("averageSalaryByLanguage", averageSalaryByLanguage);
        model.addAttribute("totalAverageSalary", totalAverageSalary);

        return "vacancy-stats";
    }
}