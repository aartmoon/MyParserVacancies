package com.example.service;

import com.example.model.VacancyEntity;
import com.example.model.VacancyModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class VacancyMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    public VacancyEntity toEntity(VacancyModel model) {
        LocalDateTime publishedAt = null;
        if (model.getPublishedAt() != null && !model.getPublishedAt().isEmpty()) {
            try {
                publishedAt = LocalDateTime.parse(model.getPublishedAt(), DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("Failed to parse date: " + model.getPublishedAt() + ". Error: " + e.getMessage());
            }
        }

        return new VacancyEntity(
                null,
                model.getTitle(),
                model.getSalary(),
                model.getLink(),
                model.getCompany(),
                model.getCity(),
                model.getRequirement(),
                model.getResponsibility(),
                publishedAt
        );
    }

    public VacancyModel toModel(VacancyEntity entity) {
        String publishedAtStr = null;
        if (entity.getPublishedAt() != null) {
            try {
                publishedAtStr = entity.getPublishedAt().format(DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.err.println("Failed to format date: " + entity.getPublishedAt() + ". Error: " + e.getMessage());
            }
        }

        return new VacancyModel(
            entity.getTitle(),
            entity.getSalary(),
            entity.getLink(),
            entity.getCompany(),
            entity.getCity(),
            entity.getRequirement(),
            entity.getResponsibility(),
            publishedAtStr
        );
    }
} 