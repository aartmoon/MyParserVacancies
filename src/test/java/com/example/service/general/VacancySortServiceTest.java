package com.example.service.general;

import com.example.model.Vacancy;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class VacancySortServiceTest {
    @Test
    void testSortBySalaryAscending() {
        Vacancy v1 = Vacancy.builder().title("A").salaryFrom(100).salaryTo(200).build();
        Vacancy v2 = Vacancy.builder().title("B").salaryFrom(200).salaryTo(300).build();
        Vacancy v3 = Vacancy.builder().title("C").salaryFrom(null).salaryTo(null).build();
        Vacancy v4 = Vacancy.builder().title("D").salaryFrom(100).salaryTo(300).build();
        Vacancy v5 = Vacancy.builder().title("E").salaryFrom(0).salaryTo(0).build();
        Vacancy v6 = Vacancy.builder().title("F").salaryFrom(null).salaryTo(150).build();
        Vacancy v7 = Vacancy.builder().title("G").salaryFrom(150).salaryTo(null).build();

        List<Vacancy> vacancies = Arrays.asList(v2, v3, v1, v4, v5, v6, v7);
        new VacancySortService().sortBySalary(vacancies, true);

        // A (100-200), D (100-300), G (150-null), B (200-300), F (null-150), C (null-null), E (0-0)
        assertEquals("A", vacancies.get(0).getTitle());
        assertEquals("D", vacancies.get(1).getTitle());
        assertEquals("G", vacancies.get(2).getTitle());
        assertEquals("B", vacancies.get(3).getTitle());
        assertEquals("F", vacancies.get(4).getTitle());
        assertEquals("C", vacancies.get(5).getTitle());
        assertEquals("E", vacancies.get(6).getTitle());
    }

    @Test
    void testSortBySalaryDescending() {
        Vacancy v1 = Vacancy.builder().title("A").salaryFrom(100).salaryTo(200).build();
        Vacancy v2 = Vacancy.builder().title("B").salaryFrom(200).salaryTo(300).build();
        Vacancy v3 = Vacancy.builder().title("C").salaryFrom(null).salaryTo(null).build();
        Vacancy v4 = Vacancy.builder().title("D").salaryFrom(100).salaryTo(300).build();
        Vacancy v5 = Vacancy.builder().title("E").salaryFrom(0).salaryTo(0).build();
        Vacancy v6 = Vacancy.builder().title("F").salaryFrom(null).salaryTo(150).build();
        Vacancy v7 = Vacancy.builder().title("G").salaryFrom(150).salaryTo(null).build();

        List<Vacancy> vacancies = Arrays.asList(v1, v3, v2, v4, v5, v6, v7);
        new VacancySortService().sortBySalary(vacancies, false);

        // B (200-300), D (100-300), A (100-200), F (null-150), G (150-null), C (null-null), E (0-0)
        assertEquals("B", vacancies.get(0).getTitle());
        assertEquals("D", vacancies.get(1).getTitle());
        assertEquals("A", vacancies.get(2).getTitle());
        assertEquals("F", vacancies.get(3).getTitle());
        assertEquals("G", vacancies.get(4).getTitle());
        assertEquals("C", vacancies.get(5).getTitle());
        assertEquals("E", vacancies.get(6).getTitle());
    }
}