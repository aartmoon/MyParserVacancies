package com.example.service.hh;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HhFetcherTest {

    @Mock
    private HhApi apiClient;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private VacancyLogger logger;

    @Mock
    private HhToVacancy vacancyParser;

    @InjectMocks
    private HhFetcher hhFetcher;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String language = "Java";
    private final String city = "Moscow";

    private ObjectNode makeRootWithItems(int numberOfItems) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode items = mapper.createArrayNode();
        for (int i = 0; i < numberOfItems; i++) {
            items.add(mapper.createObjectNode());
        }
        root.set("items", items);
        return root;
    }

    @BeforeEach
    void setUp() {
        // По умолчанию у нас нет существующих вакансий
        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void whenNoExistingAndNoItems_shouldReturnEmptyListAndLogSummary() throws Exception {
        // findByLanguageAndCity вернул пустой список (см. setUp)
        // API возвращает корень с пустым массивом items
        JsonNode rootPage0 = makeRootWithItems(0);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        // Ожидаем, что список пустой
        assertThat(result).isEmpty();

        // Проверяем, что были вызовы логгера
        verify(logger).logStartFetching(language, city);
        verify(logger).logExistingVacancies(0);
        verify(logger).logSearchParameters(language, city);
        verify(logger).logFetchingPage(0);
        verify(logger).logNoMoreItems(0);
        // Поскольку не было ни сохранённых, ни пропущенных вакансий, summary = (0,0,0,0)
        verify(logger).logSummary(0, 0, 0, 0);

        // Убедимся, что apiClient.fetchVacanciesPage не вызывался больше одного раза
        verify(apiClient, times(1)).fetchVacanciesPage(0, language, city);
    }

    @Test
    void whenThereAreExistingVacancies_andApiHasNoItems_shouldReturnOnlyExisting() throws Exception {
        // Предположим, что в БД уже есть две вакансии
        Vacancy existing1 = new Vacancy();
        existing1.setLink("link-existing-1");
        Vacancy existing2 = new Vacancy();
        existing2.setLink("link-existing-2");
        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(List.of(existing1, existing2));

        // API возвращает root с пустым массивом items
        JsonNode rootPage0 = makeRootWithItems(0);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        // Ожидаем, что метод вернёт именно существующие вакансии
        assertThat(result).containsExactly(existing1, existing2);

        // Проверяем, что каждый существующий вакансия был залогирован и добавлен в результат
        verify(logger).logStartFetching(language, city);
        verify(logger).logExistingVacancies(2);
        verify(logger).logSearchParameters(language, city);
        verify(logger, times(1)).logExistingVacancy(existing1);
        verify(logger, times(1)).logExistingVacancy(existing2);

        verify(logger).logFetchingPage(0);
        verify(logger).logNoMoreItems(0);
        // Ни одна новая вакансия не была сохранена или пропущена: summary = (0,0,2,2)
        verify(logger).logSummary(0, 0, 2, 2);
    }

    @Test
    void whenParserProducesNewVacancy_andSaveSucceeds_shouldSaveAndReturnIt() throws Exception {
        // Нет существующих вакансий (см. setUp)
        // Подготовим root с одним элементом в items
        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        // Настроим vacancyParser: при любом JsonNode возвращаем Vacancy с конкретной ссылкой
        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-new-1");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        // В репозитории не существует вакансии с такой ссылкой
        when(vacancyRepository.existsByLink("link-new-1")).thenReturn(false);
        // При сохранении возвращаем тот же объект
        when(vacancyRepository.save(parsedVac)).thenReturn(parsedVac);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        // Ожидаем, что в результате одна новая вакансия
        assertThat(result).hasSize(1).first().isEqualTo(parsedVac);
        // Проверяем, что у vacancyParser.parseVacancy был передан узел из items
        verify(vacancyParser, times(1)).parseVacancy(any(JsonNode.class));
        // Проверяем, что проверка существования по ссылке была выполнена
        verify(vacancyRepository).existsByLink("link-new-1");
        // Проверяем, что save() был вызван
        verify(vacancyRepository).save(parsedVac);
        // Логгируем сохранённую вакансию
        verify(logger).logSavedVacancy(parsedVac);
        // Summary: saved=1, skipped=0, existingDbSize=0, totalReturned=1
        verify(logger).logSummary(1, 0, 0, 1);
    }

    @Test
    void whenParserProducesExistingByLink_shouldSkipAndLogExistingTrue() throws Exception {
        // Нет существующих вакансий (см. setUp)
        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-already");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        // Репозиторий сообщает, что такая ссылка уже есть
        when(vacancyRepository.existsByLink("link-already")).thenReturn(true);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        // Поскольку вакансия «существующая», никуда не сохраняется и не попадает в result
        assertThat(result).isEmpty();
        // Проверяем, что save() НЕ вызывался
        verify(vacancyRepository, never()).save(any());
        // Проверяем, что был лог существующей вакансии с флагом true
        verify(logger).logExistingVacancy(parsedVac, true);
        // Summary: saved=0, skipped=1, existingDbSize=0, totalReturned=0
        verify(logger).logSummary(0, 1, 0, 0);
    }

    @Test
    void whenSaveThrowsDataIntegrityViolation_shouldLogFailureAndNotAddToResult() throws Exception {
        // Нет существующих вакансий (см. setUp)
        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-wrong");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        // В репозитории нет такой ссылки
        when(vacancyRepository.existsByLink("link-wrong")).thenReturn(false);
        // При попытке save бросаем исключение
        when(vacancyRepository.save(parsedVac))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        // Поскольку save бросил исключение, в result ничего нет
        assertThat(result).isEmpty();

        // Проверяем, что метод логирования ошибки был вызван с правильными аргументами
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger).logFailedToSave(linkCaptor.capture(), msgCaptor.capture());
        assertThat(linkCaptor.getValue()).isEqualTo("link-wrong");
        assertThat(msgCaptor.getValue()).contains("duplicate key");

        // Summary: saved=0, skipped=0, existingDbSize=0, totalReturned=0
        verify(logger).logSummary(0, 0, 0, 0);
    }
}
