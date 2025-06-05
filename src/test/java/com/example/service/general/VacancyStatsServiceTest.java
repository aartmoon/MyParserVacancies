package com.example.service.general;

import com.example.model.Vacancy;
import com.example.service.general.VacancyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VacancyStatsServiceTest {

    @Mock
    private List<VacancyFetcher> vacancyFetchers;

    private VacancyService vacancyService;
    private VacancyStatsService statsService;

    @BeforeEach
    void setUp() {
        vacancyService = Mockito.mock(VacancyService.class);
        statsService = new VacancyStatsService(vacancyService);
    }

    @Test
    void testGetAverageSalaryByLanguageBasicCase() {
        /*
         Java: salaryFrom=1000, salaryTo=2000  → среднее = (1000+2000)/2 = 1500
         Java: salaryFrom=1500, salaryTo=null  → среднее = 1500
         Python: salaryFrom=null, salaryTo=3000 → среднее = 3000
         Python: salaryFrom=2000, salaryTo=4000 → среднее = (2000+4000)/2 = 3000
         Go:  salaryFrom=null, salaryTo=null   → фильтруется (игнорируется)
         */
        Vacancy v1 = new Vacancy();
        v1.setLanguage("Java");
        v1.setSalaryFrom(1000);
        v1.setSalaryTo(2000);

        Vacancy v2 = new Vacancy();
        v2.setLanguage("Java");
        v2.setSalaryFrom(1500);
        v2.setSalaryTo(null);

        Vacancy v3 = new Vacancy();
        v3.setLanguage("Python");
        v3.setSalaryFrom(null);
        v3.setSalaryTo(3000);

        Vacancy v4 = new Vacancy();
        v4.setLanguage("Python");
        v4.setSalaryFrom(2000);
        v4.setSalaryTo(4000);

        Vacancy v5 = new Vacancy();
        v5.setLanguage("Go");
        v5.setSalaryFrom(null);
        v5.setSalaryTo(null);

        List<Vacancy> list = Arrays.asList(v1, v2, v3, v4, v5);
        when(vacancyService.getVacancies(null, null, true)).thenReturn(list);

        Map<String, Double> result = statsService.getAverageSalaryByLanguage();

        // Ожидаемые средние:
        // Java: (1500 + 1500) / 2 = 1500.0
        // Python: (3000 + 3000) / 2 = 3000.0
        assertEquals(2, result.size());
        assertEquals(1500.0, result.get("Java"), 0.0001);
        assertEquals(3000.0, result.get("Python"), 0.0001);
        assertFalse(result.containsKey("Go"), "Вакансии без salaryFrom и salaryTo не должны учитываться");
    }

    @Test
    void testGetAverageSalaryByLanguageEmptyList() {
        when(vacancyService.getVacancies(null, null, true)).thenReturn(Collections.emptyList());
        Map<String, Double> result = statsService.getAverageSalaryByLanguage();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Для пустого списка вакансий карта должна быть пустой");
    }

    @Test
    void testGetAverageSalaryByLanguageAllNullSalaries() {

        // Если у всех вакансий salaryFrom и salaryTo == null,
        // после фильтрации получится пустой поток

        Vacancy v1 = new Vacancy();
        v1.setLanguage("C++");
        v1.setSalaryFrom(null);
        v1.setSalaryTo(null);

        Vacancy v2 = new Vacancy();
        v2.setLanguage("Rust");
        v2.setSalaryFrom(null);
        v2.setSalaryTo(null);

        List<Vacancy> list = Arrays.asList(v1, v2);
        when(vacancyService.getVacancies(null, null, true)).thenReturn(list);

        Map<String, Double> result = statsService.getAverageSalaryByLanguage();
        assertNotNull(result);
        assertTrue(result.isEmpty(), "null => null");
    }

    @Test
    void testGetTotalAverageSalaryNonEmptyMap() {

        // Java → 1500.0, Python → 3000.0, JavaScript → 2000.0
        Map<String, Double> avgMap = Map.of(
                "Java", 1500.0,
                "Python", 3000.0,
                "JavaScript", 2000.0
        );
        // Общее среднее = (1500 + 3000 + 2000) / 3 = 2166.666
        double totalAvg = statsService.getTotalAverageSalary(avgMap);
        assertEquals((1500.0 + 3000.0 + 2000.0) / 3.0, totalAvg, 0.0001);
    }

    @Test
    void testGetTotalAverageSalaryEmptyMap() {
        double totalAvg = statsService.getTotalAverageSalary(Collections.emptyMap());
        assertEquals(0.0, totalAvg, 0.0001, "Null map => 0.0 average salary");
    }
}
