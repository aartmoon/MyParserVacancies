package com.example.service.general;

import com.example.model.Vacancy;
import java.util.List;

public interface VacancyFetcher {
    List<Vacancy> fetchVacancies(String language, String city) throws Exception;
}