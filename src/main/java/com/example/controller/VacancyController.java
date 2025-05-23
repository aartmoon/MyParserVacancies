package com.example.controller;

import com.example.model.Vacancy;
import com.example.service.general.VacancyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/vacancies")
public class VacancyController {
    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
    public String showVacancies(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "false") boolean withSalary,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            Model model) {

       if (refresh) {
            try {
                vacancyService.refreshVacancies(language, city);
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка при получении вакансий: " + e.getMessage());
                return "error";
            }
        }

        List<Vacancy> vacancies = vacancyService.getVacancies(language, city, withSalary);

        model.addAttribute("vacancies", vacancies);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedCity", city);
        model.addAttribute("withSalary", withSalary);
        return "vacancies";
    }
}