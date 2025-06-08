package com.example.service.trudvsem;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyLogger;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class TrudVsemToVacancyTest {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private TrudVsemRegionMapper regionMapper;

    @Mock
    private VacancyLogger logger;

    @InjectMocks
    private TrudVsemToVacancy converter;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void returnsNullWhenMissingJobNameOrUrl() {
        JsonObject obj = new JsonObject();
        obj.addProperty("vac_url", "http://example.com");
        assertNull(converter.parseVacancy(obj));

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("job-name", "Developer");
        assertNull(converter.parseVacancy(obj2));
    }

    @Test
    void returnsNullWhenLinkAlreadyExists() {
        JsonObject vac = new JsonObject();
        vac.addProperty("job-name", "Dev");
        vac.addProperty("vac_url", "http://link");
        when(vacancyRepository.existsByLink("http://link")).thenReturn(true);

        assertNull(converter.parseVacancy(vac));
        verify(vacancyRepository).existsByLink("http://link");
    }

    @Test
    void parsesAllFieldsCorrectly() {
        JsonObject wrapper = new JsonObject();
        JsonObject vacObj = new JsonObject();
        wrapper.add("vacancy", vacObj);
        vacObj.addProperty("job-name", "Dev");
        vacObj.addProperty("vac_url", "http://link");

        JsonObject comp = new JsonObject(); comp.addProperty("name", "MyCompany");
        vacObj.add("company", comp);

        JsonObject region = new JsonObject(); region.addProperty("name", "Moscow Region");
        vacObj.add("region", region);
        when(regionMapper.extractCityFromRegion("Moscow Region")).thenReturn("Moscow");

        vacObj.addProperty("salary_min", 100000);
        vacObj.addProperty("salary_max", 200000);

        vacObj.addProperty("duty", "Do things");

        JsonObject req = new JsonObject();
        req.addProperty("education", "Higher");
        req.addProperty("experience", 3);
        vacObj.add("requirement", req);

        vacObj.addProperty("creation-date", "2025-06-01");

        when(vacancyRepository.existsByLink("http://link")).thenReturn(false);

        Vacancy vac = converter.parseVacancy(wrapper);
        assertThat(vac).isNotNull();
        assertThat(vac.getTitle()).isEqualTo("Dev");
        assertThat(vac.getLink()).isEqualTo("http://link");
        assertThat(vac.getCompany()).isEqualTo("MyCompany");
        assertThat(vac.getCity()).isEqualTo("Moscow");
        assertThat(vac.getSalaryFrom()).isEqualTo(100000);
        assertThat(vac.getSalaryTo()).isEqualTo(200000);
        assertThat(vac.getCurrency()).isEqualTo("RUB");
        assertThat(vac.getResponsibility()).isEqualTo("Do things");
        assertThat(vac.getRequirement()).contains("Образование: Higher").contains("Опыт работы: 3 лет");
        assertThat(vac.getPublishedAt()).isEqualTo(LocalDate.parse("2025-06-01").atStartOfDay());
    }

    @Test
    void handlesInvalidDateAndLogsError() {
        JsonObject vacObj = new JsonObject();
        vacObj.addProperty("job-name", "Dev");
        vacObj.addProperty("vac_url", "http://link");
        vacObj.addProperty("creation-date", "invalid-date");
        when(vacancyRepository.existsByLink("http://link")).thenReturn(false);

        Vacancy vac = converter.parseVacancy(vacObj);
        assertThat(vac).isNotNull();
        assertThat(vac.getPublishedAt()).isNull();
        verify(logger).logDateParseError(eq("invalid-date"), any(Exception.class));
    }

    @Test
    void catchesExceptionsAndLogsFailedToSave() {
        JsonObject vacObj = mock(JsonObject.class);
        when(vacObj.has("vacancy")).thenThrow(new RuntimeException("oops"));

        Vacancy result = converter.parseVacancy(vacObj);
        assertNull(result);
        verify(logger).logFailedToSave(eq("unknown"), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains("oops");
    }
}
