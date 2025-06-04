package com.example.service.general;

import com.example.model.Vacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VacancyCleanerTest {

    private VacancyCleaner vacancyCleaner;

    @BeforeEach
    void setUp() {
        vacancyCleaner = new VacancyCleaner();
    }

    @Test
    void shouldRemoveHtmlTagsFromSingleVacancy() {
        Vacancy vacancy = new Vacancy();
        vacancy.setRequirement("Required: <highlighttext>Java Developer</highlighttext>");
        vacancy.setResponsibility("Responsibility: <div>Write <b>clean</b> code</div>");

        Vacancy cleaned = vacancyCleaner.clean(vacancy);

        assertEquals("Required: Java Developer", cleaned.getRequirement());
        assertEquals("Responsibility: Write clean code", cleaned.getResponsibility());
    }

    @Test
    void shouldHandleVacancyWithNullFields() {
        Vacancy vacancy = new Vacancy();
        vacancy.setRequirement(null);
        vacancy.setResponsibility(null);

        Vacancy cleaned = vacancyCleaner.clean(vacancy);

        assertNull(cleaned.getRequirement());
        assertNull(cleaned.getResponsibility());
    }

    @Test
    void shouldCleanAllVacanciesInList() {
        Vacancy v1 = new Vacancy();
        v1.setRequirement("<p>Test1</p>");
        v1.setResponsibility("<span>Resp1</span>");

        Vacancy v2 = new Vacancy();
        v2.setRequirement("NoTags");
        v2.setResponsibility("<em>Resp2</em>");

        List<Vacancy> rawList = Arrays.asList(v1, v2);
        List<Vacancy> cleanedList = vacancyCleaner.clean(rawList);

        assertEquals(2, cleanedList.size());

        Vacancy cleaned1 = cleanedList.get(0);
        assertEquals("Test1", cleaned1.getRequirement());
        assertEquals("Resp1", cleaned1.getResponsibility());

        Vacancy cleaned2 = cleanedList.get(1);
        assertEquals("NoTags", cleaned2.getRequirement());
        assertEquals("Resp2", cleaned2.getResponsibility());
    }
}
