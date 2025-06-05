package com.example.config;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final List<String> LANGUAGES = List.of("Java", "Python", "PHP");
    public static final List<String> CITIES = List.of("Москва", "Санкт-Петербург", "Екатеринбург");
    
    public static final Map<String, Integer> CITY_AREAS_HH = Map.of(
        "Москва", 1,
        "Санкт-Петербург", 2,
        "Екатеринбург", 3
    );

    public static final int MAX_PAGE = 1;

    public static final Map<String, Integer> CURRENCY_TO_RUB = Map.of(
        "RUR", 1,
        "RUB", 1,
        "USD", 90,
        "EUR", 100
    );

    public static final Map<String, String> CITY_TO_REGION_CODE_TRUDVSEM = Map.of(
            "Москва", "77",
            "Санкт-Петербург", "78",
            "Новосибирск", "54",
            "Екатеринбург", "66",
            "Казань", "16",
            "Нижний Новгород", "52"
    );
    public static final int MAX_PAGES_TRUDVSEM = 10;
    public static final int PAGE_SIZE_TRUDVSEM = 30;

}