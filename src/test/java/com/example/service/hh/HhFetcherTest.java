//package com.example.service.hh;
//
//import com.example.model.Vacancy;
//import com.example.repository.VacancyRepository;
//import com.example.service.general.VacancyLogger;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.dao.DataIntegrityViolationException;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class HhFetcherTest {
//
//    @Mock
//    private HhApi apiClient;
//
//    @Mock
//    private VacancyRepository vacancyRepository;
//
//    @Mock
//    private VacancyLogger logger;
//
//    @Mock
//    private HhToVacancy vacancyParser;
//
//    private HhFetcher hhFetcher;
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        hhFetcher = new HhFetcher(apiClient, vacancyRepository, logger, vacancyParser);
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    void fetchVacancies_NewVacancies() throws Exception {
//        // Arrange
//        String language = "Java";
//        String city = "Москва";
//        List<Vacancy> existingVacancies = Arrays.asList(
//                createVacancy("1", "Existing Job 1"),
//                createVacancy("2", "Existing Job 2")
//        );
//
//        JsonNode root = createJsonResponse(
//                createVacancyNode("3", "New Job 1"),
//                createVacancyNode("4", "New Job 2")
//        );
//
//        when(vacancyRepository.findByLanguageAndCity(language, city)).thenReturn(existingVacancies);
//        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(root);
//        when(vacancyParser.parseVacancy(any())).thenAnswer(invocation -> {
//            JsonNode node = invocation.getArgument(0);
//            return createVacancy(node.get("id").asText(), node.get("name").asText());
//        });
//        when(vacancyRepository.existsByLink(any())).thenReturn(false);
//
//        // Act
//        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);
//
//        // Assert
//        assertEquals(4, result.size());
//        verify(vacancyRepository, times(2)).save(any(Vacancy.class));
//        verify(logger).logSummary(2, 0, 2, 4);
//    }
//
//    @Test
//    void fetchVacancies_AllExisting() throws Exception {
//        // Arrange
//        String language = "Java";
//        String city = "Москва";
//        List<Vacancy> existingVacancies = Arrays.asList(
//                createVacancy("1", "Existing Job 1"),
//                createVacancy("2", "Existing Job 2")
//        );
//
//        JsonNode root = createJsonResponse(
//                createVacancyNode("1", "Existing Job 1"),
//                createVacancyNode("2", "Existing Job 2")
//        );
//
//        when(vacancyRepository.findByLanguageAndCity(language, city)).thenReturn(existingVacancies);
//        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(root);
//        when(vacancyParser.parseVacancy(any())).thenAnswer(invocation -> {
//            JsonNode node = invocation.getArgument(0);
//            return createVacancy(node.get("id").asText(), node.get("name").asText());
//        });
//        when(vacancyRepository.existsByLink(any())).thenReturn(true);
//
//        // Act
//        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);
//
//        // Assert
//        assertEquals(2, result.size());
//        verify(vacancyRepository, never()).save(any(Vacancy.class));
//        verify(logger).logSummary(0, 2, 2, 2);
//    }
//
//    @Test
//    void fetchVacancies_EmptyResponse() throws Exception {
//        // Arrange
//        String language = "Java";
//        String city = "Москва";
//        List<Vacancy> existingVacancies = Arrays.asList(
//                createVacancy("1", "Existing Job 1")
//        );
//
//        JsonNode root = createEmptyJsonResponse();
//
//        when(vacancyRepository.findByLanguageAndCity(language, city)).thenReturn(existingVacancies);
//        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(root);
//
//        // Act
//        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);
//
//        // Assert
//        assertEquals(1, result.size());
//        verify(vacancyRepository, never()).save(any(Vacancy.class));
//        verify(logger).logSummary(0, 0, 1, 1);
//    }
//
//    @Test
//    void fetchVacancies_SaveError() throws Exception {
//        // Arrange
//        String language = "Java";
//        String city = "Москва";
//        List<Vacancy> existingVacancies = List.of();
//
//        JsonNode root = createJsonResponse(
//                createVacancyNode("1", "New Job 1")
//        );
//
//        when(vacancyRepository.findByLanguageAndCity(language, city)).thenReturn(existingVacancies);
//        when(apiClient.fetchVacanciesPage(0, language, city)).thenReturn(root);
//        when(vacancyParser.parseVacancy(any())).thenReturn(createVacancy("1", "New Job 1"));
//        when(vacancyRepository.existsByLink(any())).thenReturn(false);
//        when(vacancyRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate entry"));
//
//        // Act
//        List<Vacancy> result = hhFetcher.fetchVacancies(language, city);
//
//        // Assert
//        assertTrue(result.isEmpty());
//        verify(logger).logFailedToSave(anyString(), anyString());
//        verify(logger).logSummary(0, 0, 0, 0);
//    }
//
//    private Vacancy createVacancy(String id, String title) {
//        return Vacancy.builder()
//                .id(Long.valueOf(id))
//                .title(title)
//                .link("https://hh.ru/vacancy/" + id)
//                .build();
//    }
//
//    private JsonNode createVacancyNode(String id, String name) {
//        ObjectNode node = objectMapper.createObjectNode();
//        node.put("id", id);
//        node.put("name", name);
//        return node;
//    }
//
//    private JsonNode createJsonResponse(JsonNode... items) {
//        ObjectNode root = objectMapper.createObjectNode();
//        ArrayNode itemsArray = root.putArray("items");
//        for (JsonNode item : items) {
//            itemsArray.add(item);
//        }
//        return root;
//    }
//
//    private JsonNode createEmptyJsonResponse() {
//        ObjectNode root = objectMapper.createObjectNode();
//        root.putArray("items");
//        return root;
//    }
//}