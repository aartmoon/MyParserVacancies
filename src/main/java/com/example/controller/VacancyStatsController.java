package com.example.controller;

import com.example.service.general.VacancyStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/vacancy-stats")
@RequiredArgsConstructor
public class VacancyStatsController {

    private final VacancyStatsService statsService;

    @GetMapping
    public String getStats(Model model) {

        Map<String, Double> averageSalaryByLanguage = statsService.getAverageSalaryByLanguage();

        double totalAverageSalary = statsService.getTotalAverageSalary(averageSalaryByLanguage);

        model.addAttribute("averageSalaryByLanguage", averageSalaryByLanguage);
        model.addAttribute("totalAverageSalary", totalAverageSalary);

        return "vacancy-stats";
    }
}
