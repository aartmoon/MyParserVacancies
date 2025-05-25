package com.example.config;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final List<String> LANGUAGES = List.of("Java", "Python", "PHP");
    public static final List<String> CITIES = List.of("Москва", "Санкт-Петербург", "Екатеринбург");
    
    public static final Map<String, Integer> CITY_AREAS = Map.of(
        "Москва", 1,
        "Санкт-Петербург", 2,
        "Екатеринбург", 3
    );

    public static final int MAX_PAGE = 100;

    public static final Map<String, Double> CURRENCY_TO_RUB = Map.of(
        "RUR", 1.0,
        "RUB", 1.0,
        "USD", 90.0,
        "EUR", 100.0
    );
}