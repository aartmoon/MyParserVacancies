package com.example.controller;

import com.example.model.VacancyEntity;
import com.example.service.VacancyStatisticsService;
import com.example.service.VacancyParserHH;
import com.example.repository.VacancyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class StatisticController {

    private final VacancyRepository vacancyRepository;
    private final VacancyStatisticsService statisticsService;
    private final VacancyParserHH vacancyParser;

    public StatisticController(
            VacancyRepository vacancyRepository,
            VacancyStatisticsService statisticsService,
            VacancyParserHH vacancyParser) {
        this.vacancyRepository = vacancyRepository;
        this.statisticsService = statisticsService;
        this.vacancyParser = vacancyParser;
    }

    @GetMapping("/statistic")
    public String showStatistics(
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            Model model) {
        
        if (refresh) {
            try {
                if (language == null || language.isEmpty()) {
                    vacancyParser.fetchVacancies("Java", city);
                    vacancyParser.fetchVacancies("Python", city);
                    vacancyParser.fetchVacancies("PHP", city);
                } else {
                    vacancyParser.fetchVacancies(language, city);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка при обновлении данных: " + e.getMessage());
                return "error";
            }
        }

        List<VacancyEntity> allVacancies = vacancyRepository.findAll();
        
        if (city != null && !city.isEmpty()) {
            allVacancies = allVacancies.stream()
                    .filter(v -> v.getCity().equals(city))
                    .toList();
        }

        Map<String, Double> avgSalaries = statisticsService.calculateAverageSalaries(allVacancies, language, city);
        
        model.addAttribute("avgSalaries", avgSalaries);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedCity", city);
        return "statistic";
    }
} 