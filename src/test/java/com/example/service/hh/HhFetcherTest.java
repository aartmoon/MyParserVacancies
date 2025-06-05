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
    private final String city = "Москва";

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
        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void whenNoItemsShouldReturnEmptyList() throws Exception {

        JsonNode rootPage0 = makeRootWithItems(0);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        assertThat(result).isEmpty();

        verify(logger).logStartFetching(language, city);
        verify(logger).logExistingVacancies(0);
        verify(logger).logSearchParameters(language, city);
        verify(logger).logFetchingPage(0);
        verify(logger).logNoMoreItems(0);

        verify(logger).logSummary(0, 0, 0, 0);

        verify(apiClient, times(1)).fetchVacanciesPage(0, language, city);
    }

    @Test
    void whenAreExisting() throws Exception {
        Vacancy existing1 = new Vacancy();
        existing1.setLink("link-existing-1");
        Vacancy existing2 = new Vacancy();
        existing2.setLink("link-existing-2");
        when(vacancyRepository.findByLanguageAndCity(language, city))
                .thenReturn(List.of(existing1, existing2));

        JsonNode rootPage0 = makeRootWithItems(0);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        assertThat(result).containsExactly(existing1, existing2);

        verify(logger).logStartFetching(language, city);
        verify(logger).logExistingVacancies(2);
        verify(logger).logSearchParameters(language, city);
        verify(logger, times(1)).logExistingVacancy(existing1);
        verify(logger, times(1)).logExistingVacancy(existing2);

        verify(logger).logFetchingPage(0);
        verify(logger).logNoMoreItems(0);

        verify(logger).logSummary(0, 0, 2, 2);
    }

    @Test
    void newVacancies() throws Exception {
        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-new-1");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        when(vacancyRepository.existsByLink("link-new-1")).thenReturn(false);
        when(vacancyRepository.save(parsedVac)).thenReturn(parsedVac);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        assertThat(result).hasSize(1).first().isEqualTo(parsedVac);

        verify(vacancyParser, times(1)).parseVacancy(any(JsonNode.class));

        verify(vacancyRepository).existsByLink("link-new-1");

        verify(vacancyRepository).save(parsedVac);

        verify(logger).logSavedVacancy(parsedVac);

        verify(logger).logSummary(1, 0, 0, 1);
    }

    @Test
    void whenHasLinkShouldSkip() throws Exception {

        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-already");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        when(vacancyRepository.existsByLink("link-already")).thenReturn(true);

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        assertThat(result).isEmpty();

        verify(vacancyRepository, never()).save(any());
        verify(logger).logExistingVacancy(parsedVac, true);
        verify(logger).logSummary(0, 1, 0, 0);
    }

    @Test
    void whenWrongLink() throws Exception {
        JsonNode rootPage0 = makeRootWithItems(1);
        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(rootPage0);

        Vacancy parsedVac = new Vacancy();
        parsedVac.setLink("link-wrong");
        when(vacancyParser.parseVacancy(any(JsonNode.class))).thenReturn(parsedVac);

        when(vacancyRepository.existsByLink("link-wrong")).thenReturn(false);
        when(vacancyRepository.save(parsedVac))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);

        assertThat(result).isEmpty();

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger).logFailedToSave(linkCaptor.capture(), msgCaptor.capture());
        assertThat(linkCaptor.getValue()).isEqualTo("link-wrong");
        assertThat(msgCaptor.getValue()).contains("duplicate key");

        verify(logger).logSummary(0, 0, 0, 0);
    }
}
