package com.example.controller;

import com.example.model.Vacancy;
import com.example.service.general.VacancyService;
import com.example.service.general.VacancySortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/vacancies")
@RequiredArgsConstructor
public class VacancyController {
    private final VacancyService vacancyService;
    private final VacancySortService vacancySortService;

    @GetMapping
    public String showVacancies(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "false") boolean withSalary,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            @RequestParam(required = false) String sort,
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

        // Сортировка по зарплате только если withSalary=true
        if (withSalary && sort != null) {
            if ("salaryAsc".equals(sort)) {
                vacancySortService.sortBySalary(vacancies, true);
            } else if ("salaryDesc".equals(sort)) {
                vacancySortService.sortBySalary(vacancies, false);
            }
        }

        model.addAttribute("vacancies", vacancies);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedCity", city);
        model.addAttribute("withSalary", withSalary);
        model.addAttribute("sort", sort);
        return "vacancies";
    }
}