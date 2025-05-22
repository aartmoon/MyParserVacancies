package com.example.service;

import com.example.model.VacancyModel;
import java.util.List;

public interface VacancyParser {
    List<VacancyModel> fetchVacancies(String language) throws Exception;
}
