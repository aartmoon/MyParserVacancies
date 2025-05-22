package com.example.service;

import com.example.model.VacancyEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VacancyStatisticsService {

    public Map<String, Double> calculateAverageSalaries(List<VacancyEntity> vacancies, String selectedLanguage, String selectedCity) {
        return vacancies.stream()
                .filter(v -> v.getSalary() != null && !v.getSalary().equals("не указано"))
                .filter(v -> selectedLanguage == null || matchesLanguage(v.getTitle(), selectedLanguage))
                .filter(v -> selectedCity == null || v.getCity().equals(selectedCity))
                .collect(Collectors.groupingBy(
                        v -> selectedLanguage != null ? selectedLanguage : extractLanguage(v.getTitle()),
                        Collectors.averagingDouble(v -> parseSalary(v.getSalary()))
                ));
    }

    private boolean matchesLanguage(String title, String selectedLanguage) {
        String lowerTitle = title.toLowerCase();
        switch (selectedLanguage.toLowerCase()) {
            case "php":
                return lowerTitle.contains("php");
            case "java":
                return lowerTitle.contains("java");
            case "python":
                return lowerTitle.contains("python");
            default:
                return false;
        }
    }

    private String extractLanguage(String title) {
        String lowerTitle = title.toLowerCase();
        
        if (lowerTitle.contains("php")) {
            return "PHP";
        }
        if (lowerTitle.contains("java")) {
            return "Java";
        }
        if (lowerTitle.contains("python")) {
            return "Python";
        }
        return null;
    }

    private double parseSalary(String salary) {
        if (salary == null || salary.isEmpty() || salary.equals("не указано")) {
            return 0.0;
        }
        try {
            String[] parts = salary.split("-");
            String firstPart = parts[0].trim();
            return Double.parseDouble(firstPart.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
} 