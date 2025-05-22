package com.example.controller;

import com.example.model.VacancyEntity;
import com.example.service.VacancyParser;
import com.example.service.VacancyParserHH;
import com.example.service.VacancyStatisticsService;
import com.example.repository.VacancyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vacancies")
public class VacancyController {

    private final VacancyParser vacancyParser;
    private final VacancyRepository vacancyRepository;
    private final VacancyStatisticsService statisticsService;

    public VacancyController(
            VacancyParser vacancyParser,
            VacancyRepository vacancyRepository,
            VacancyStatisticsService statisticsService) {
        this.vacancyParser = vacancyParser;
        this.vacancyRepository = vacancyRepository;
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String showVacancies(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "false") boolean withSalary,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            HttpServletRequest request,
            Model model) {
        
        List<VacancyEntity> vacancies;
        
        if (refresh) {
            try {
                if (language != null && !language.isEmpty()) {
                    ((VacancyParserHH) vacancyParser).fetchVacancies(language, city);
                } else {
                    ((VacancyParserHH) vacancyParser).fetchVacancies("Java", city);
                    ((VacancyParserHH) vacancyParser).fetchVacancies("Python", city);
                    ((VacancyParserHH) vacancyParser).fetchVacancies("PHP", city);
                }
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка при получении вакансий: " + e.getMessage());
                return "error";
            }
        }

        if ((language != null && !language.isEmpty()) || (city != null && !city.isEmpty())) {
            vacancies = vacancyRepository.findByLanguageAndCity(language, city);
        } else {
            vacancies = vacancyRepository.findAll();
        }

        List<VacancyEntity> filteredVacancies = vacancies;
        if (withSalary) {
            filteredVacancies = vacancies.stream()
                    .filter(v -> v.getSalary() != null && !v.getSalary().equals("не указано"))
                    .collect(Collectors.toList());
        }

        Map<String, Double> avgSalaries = statisticsService.calculateAverageSalaries(vacancies, language, city);
        
        String currentUrl = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        
        model.addAttribute("vacancies", filteredVacancies);
        model.addAttribute("avgSalaries", avgSalaries);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedCity", city);
        model.addAttribute("withSalary", withSalary);
        model.addAttribute("currentUrl", currentUrl);
        
        return "vacancies";
    }
}