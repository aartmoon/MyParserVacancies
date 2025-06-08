package com.example.service.hh;

import com.example.model.Vacancy;
import com.example.service.general.VacancyLogger;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class HhToVacancy {
    private final VacancyLogger logger;

    public Vacancy parseVacancy(JsonNode vacancyNode) {
        String title = HhGetterFromBio.getText(vacancyNode, "name");
        JsonNode salaryNode = vacancyNode.path("salary");
        String currency = HhGetterFromBio.getText(salaryNode, "currency", "RUR");
        Integer salaryFrom = HhGetterFromBio.parseSalaryField(salaryNode, "from");
        Integer salaryTo = HhGetterFromBio.parseSalaryField(salaryNode, "to");
        salaryFrom = HhGetterFromBio.convertToRub(salaryFrom, currency);
        salaryTo = HhGetterFromBio.convertToRub(salaryTo, currency);
        String link = HhGetterFromBio.getText(vacancyNode, "alternate_url", "#");
        String company = HhGetterFromBio.getText(vacancyNode.path("employer"), "name");
        String city = HhGetterFromBio.getText(vacancyNode.path("area"), "name");

        JsonNode snippet = vacancyNode.path("snippet");
        String requirement = HhGetterFromBio.getText(snippet, "requirement");
        String responsibility = HhGetterFromBio.getText(snippet, "responsibility");
        String publishedAtStr = HhGetterFromBio.getText(vacancyNode, "published_at");

        LocalDateTime publishedAt = null;
        if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(publishedAtStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
                publishedAt = offsetDateTime.toLocalDateTime();
            } catch (DateTimeParseException e) {
                logger.logDateParseError(publishedAtStr, e);
            }
        }

        return Vacancy.builder()
                .title(title)
                .salaryFrom(salaryFrom)
                .salaryTo(salaryTo)
                .currency(currency)
                .link(link)
                .company(company)
                .city(city)
                .requirement(requirement)
                .responsibility(responsibility)
                .publishedAt(publishedAt)
                .build();
    }
}