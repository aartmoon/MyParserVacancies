package com.example.service.hh;

import com.example.model.Vacancy;
import com.example.service.general.VacancyLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HhToVacancyTest {

    @Mock
    private VacancyLogger logger;

    @InjectMocks
    private HhToVacancy parser;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void validJsond() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Java Developer\",\n" +
                "  \"salary\": {\n" +
                "    \"from\": 100,\n" +
                "    \"to\": 200,\n" +
                "    \"currency\": \"RUR\"\n" +
                "  },\n" +
                "  \"alternate_url\": \"https://hh.ru/vacancy/12345\",\n" +
                "  \"employer\": { \"name\": \"Acme Corp\" },\n" +
                "  \"area\": { \"name\": \"Москва\" },\n                \"snippet\": {\n" +
                "    \"requirement\": \"Опыт работы от 3 лет\",\n" +
                "    \"responsibility\": \"Разработка backend\"\n" +
                "  },\n" +
                "  \"published_at\": \"2025-06-01T12:00:00+0000\"\n" +
                "}";
        JsonNode vacancyNode = mapper.readTree(json);

        Vacancy result = parser.parseVacancy(vacancyNode);

        assertThat(result.getTitle()).isEqualTo("Java Developer");

        // Поскольку валюта "RUR", конвертация вернёт те же числа
        assertThat(result.getSalaryFrom()).isEqualTo(100);
        assertThat(result.getSalaryTo()).isEqualTo(200);
        assertThat(result.getCurrency()).isEqualTo("RUR");

        assertThat(result.getLink()).isEqualTo("https://hh.ru/vacancy/12345");
        assertThat(result.getCompany()).isEqualTo("Acme Corp");
        assertThat(result.getCity()).isEqualTo("Москва");
        assertThat(result.getRequirement()).isEqualTo("Опыт работы от 3 лет");
        assertThat(result.getResponsibility()).isEqualTo("Разработка backend");

        LocalDateTime expected = OffsetDateTime.parse(
                "2025-06-01T12:00:00+0000",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        ).toLocalDateTime();

        assertThat(result.getPublishedAt()).isEqualTo(expected);

        verify(logger, never()).logDateParseError(anyString(), any(Exception.class));
    }

    @Test
    void invalidJson() throws Exception {
        String json = "{\n" +
                "  \"name\": \"Python Developer\",\n" +
                "  \"salary\": { \"from\": 50, \"to\": 150, \"currency\": \"RUR\" },\n" +
                "  \"alternate_url\": \"https://hh.ru/vacancy/67890\",\n" +
                "  \"employer\": { \"name\": \"Beta Inc\" },\n" +
                "  \"area\": { \"name\": \"Санкт-Петербург\" },\n" +
                "  \"snippet\": { \"requirement\": \"Опыт от 2 лет\", \"responsibility\": \"Поддержка проектов\" },\n" +
                "  \"published_at\": \"2025-13-01T99:99:99+0000\"  \n" + // явный неверный формат даты
                "}";
        JsonNode vacancyNode = mapper.readTree(json);

        Vacancy result = parser.parseVacancy(vacancyNode);

        // Все поля, кроме publishedAt, должны быть распарсены аналогично первому тесту
        assertThat(result.getTitle()).isEqualTo("Python Developer");
        assertThat(result.getSalaryFrom()).isEqualTo(50);
        assertThat(result.getSalaryTo()).isEqualTo(150);
        assertThat(result.getCurrency()).isEqualTo("RUR");
        assertThat(result.getLink()).isEqualTo("https://hh.ru/vacancy/67890");
        assertThat(result.getCompany()).isEqualTo("Beta Inc");
        assertThat(result.getCity()).isEqualTo("Санкт-Петербург");
        assertThat(result.getRequirement()).isEqualTo("Опыт от 2 лет");
        assertThat(result.getResponsibility()).isEqualTo("Поддержка проектов");

        // При некорректном формате дата должна быть null
        assertThat(result.getPublishedAt()).isNull();

        // Проверяем, что logger.logDateParseError вызван ровно один раз с ожидаемыми аргументами
        ArgumentCaptor<String> captorDate = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Exception> captorEx = ArgumentCaptor.forClass(Exception.class);
        verify(logger, times(1)).logDateParseError(captorDate.capture(), captorEx.capture());

        // Убедимся, что в лог передали именно строку с датой из JSON
        assertThat(captorDate.getValue()).isEqualTo("2025-13-01T99:99:99+0000");
        // Тип исключения — DateTimeParseException или его наследник
        assertThat(captorEx.getValue()).isInstanceOf(Exception.class);
    }
}
