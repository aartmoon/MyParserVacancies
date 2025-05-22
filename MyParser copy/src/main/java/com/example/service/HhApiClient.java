package com.example.service;

import com.example.config.HhSearchProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class HhApiClient {
    private static final String BASE_URL = "https://api.hh.ru/vacancies";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HhSearchProperties props;

    private static final Map<String, Integer> CITY_AREAS = Map.of(
            "Москва", 1,
            "Санкт-Петербург", 2,
            "Екатеринбург", 3
    );

    public HhApiClient(HttpClient httpClient, ObjectMapper objectMapper, HhSearchProperties props) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    private int getAreaCode(String city) {
        return CITY_AREAS.getOrDefault(city, 1); // По умолчанию Москва
    }

    public JsonNode fetchVacanciesPage(int page, String language, String city) throws Exception {
        String textParam = URLEncoder.encode(language, StandardCharsets.UTF_8);
        int areaCode = city != null ? getAreaCode(city) : props.getArea();

        String url = String.format(
                "%s?text=%s&search_field=%s&area=%d&salary_from=%d&currency=%s&only_with_salary=%b&per_page=%d&page=%d",
                BASE_URL,
                textParam,
                props.getSearchField(),
                areaCode,
                props.getMinSalary(),
                props.getCurrency(),
                props.isOnlyWithSalary(),
                props.getPerPage(),
                page
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "VacancyParser/1.0")
                .header("HH-User-Agent", "VacancyParser/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch vacancies: HTTP " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }
} 