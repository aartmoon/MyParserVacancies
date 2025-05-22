package com.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class VacancyModel {
    private final Map<String, String> details = new HashMap<>();
    private final String title;
    private final String salary;
    private final String link;
    private final String company;
    private final String city;
    private final String requirement;
    private final String responsibility;
    private final String publishedAt;

    public VacancyModel(String title, String salary, String link, String company, String city,
                       String requirement, String responsibility, String publishedAt) {
        this.title = title;
        this.salary = salary;
        this.link = link;
        this.company = company;
        this.city = city;
        this.requirement = requirement;
        this.responsibility = responsibility;
        this.publishedAt = publishedAt;
    }

    public VacancyModel(JsonNode vacancyNode) {

        vacancyNode.fieldNames().forEachRemaining(field -> {
            JsonNode value = vacancyNode.get(field);
            details.put(field, value.isTextual() ? value.asText() : value.toString());
        });

        this.title = vacancyNode.path("name").asText("");
        JsonNode salNode = vacancyNode.path("salary");

        if (salNode.isObject()) {
            String from = salNode.hasNonNull("from") ? salNode.get("from").asText() : "";
            String to = salNode.hasNonNull("to") ? salNode.get("to").asText() : "";
            String curr = salNode.path("currency").asText("");
            this.salary = (from + (to.isEmpty() ? "" : " - " + to) + " " + curr).trim();
        } else {
            this.salary = "не указано";
        }

        this.link = vacancyNode.path("alternate_url").asText("#");
        this.company = vacancyNode.path("employer").path("name").asText("");
        this.city = vacancyNode.path("area").path("name").asText("");

        JsonNode snippet = vacancyNode.path("snippet");
        this.requirement = snippet.path("requirement").asText("");
        this.responsibility = snippet.path("responsibility").asText("");
        this.publishedAt = vacancyNode.path("published_at").asText("");
    }

    public String getTitle() {
        return title;
    }

    public String getSalary() {
        return salary.isEmpty() ? "не указано" : salary;
    }

    public String getLink() {
        return link;
    }

    public String getCompany() {
        return company;
    }

    public String getCity() {
        return city;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}