package com.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Table(
    name = "vacancies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vacancy_link",
            columnNames = "link")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String salary;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    @Column
    private LocalDateTime publishedAt;

    private static String getText(JsonNode node, String fieldName) {
        return getText(node, fieldName, "");
    }

    private static String getText(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText(defaultValue) : defaultValue;
    }

    private static String parseSalary(JsonNode salNode) {
        if (!salNode.isObject()) {
            return "не указано";
        }

        String from = getText(salNode, "from");
        String to = getText(salNode, "to");
        String currency = getText(salNode, "currency");

        StringBuilder salary = new StringBuilder();
        if (!from.isEmpty()) {
            salary.append(from);
        }
        if (!to.isEmpty()) {
            if (salary.length() > 0) {
                salary.append(" - ");
            }
            salary.append(to);
        }
        if (!currency.isEmpty()) {
            if (salary.length() > 0) {
                salary.append(" ");
            }
            salary.append(currency);
        }

        return salary.length() > 0 ? salary.toString() : "не указано";
    }

    public static Vacancy fromJson(JsonNode vacancyNode) {
        String title = getText(vacancyNode, "name");
        String salary = parseSalary(vacancyNode.path("salary"));
        String link = getText(vacancyNode, "alternate_url", "#");
        String company = getText(vacancyNode.path("employer"), "name");
        String city = getText(vacancyNode.path("area"), "name");
        
        JsonNode snippet = vacancyNode.path("snippet");
        String requirement = getText(snippet, "requirement");
        String responsibility = getText(snippet, "responsibility");
        String publishedAtStr = getText(vacancyNode, "published_at");

        LocalDateTime publishedAt = null;
        if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(publishedAtStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
                publishedAt = offsetDateTime.toLocalDateTime();
            } catch (DateTimeParseException e) {
                // Логирование ошибки будет добавлено позже
            }
        }

        return Vacancy.builder()
            .title(title)
            .salary(salary)
            .link(link)
            .company(company)
            .city(city)
            .requirement(requirement)
            .responsibility(responsibility)
            .publishedAt(publishedAt)
            .build();
    }
} 