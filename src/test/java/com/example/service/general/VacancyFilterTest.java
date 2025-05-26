package com.example.service.general;

import com.example.model.Vacancy;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class VacancyFilterTest {

    @Test
    public void TestWithFilter() {
        Vacancy v1 = Vacancy.builder().title("A").salaryFrom(100).salaryTo(200).build();
        Vacancy v2 = Vacancy.builder().title("B").salaryFrom(null).salaryTo(300).build();
        Vacancy v3 = Vacancy.builder().title("C").salaryFrom(null).salaryTo(null).build();
        Vacancy v4 = Vacancy.builder().title("D").salaryFrom(100).salaryTo(null).build();
        Vacancy v5 = Vacancy.builder().title("E").salaryFrom(0).salaryTo(0).build();

        List<Vacancy> vacancies = Arrays.asList(v1, v2, v3, v4, v5);
        VacancyFilter filter = new VacancyFilter();
        List<Vacancy> filtered = filter.filterBySalary(vacancies);

        assertEquals(4, filtered.size());
        assertTrue(filtered.contains(v1));
        assertTrue(filtered.contains(v2));
        assertTrue(filtered.contains(v4));
        assertTrue(filtered.contains(v5));
        assertFalse(filtered.contains(v3));
    }

    @Test
    public void TestWithoutFilter() {
        Vacancy v1 = Vacancy.builder().title("A").salaryFrom(null).salaryTo(null).build();
        Vacancy v2 = Vacancy.builder().title("B").salaryFrom(null).salaryTo(null).build();
        Vacancy v3 = Vacancy.builder().title("C").salaryFrom(null).salaryTo(null).build();

        List<Vacancy> vacancies = Arrays.asList(v1, v2, v3);
        VacancyFilter filter = new VacancyFilter();
        List<Vacancy> filtered = filter.filterBySalary(vacancies);

        assertTrue(filtered.isEmpty());
    }
}
