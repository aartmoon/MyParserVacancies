package com.example.service.hh;

import com.example.config.HhSearchProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HhApiTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HhSearchProperties props;

    @Mock
    private HttpResponse<Object> httpResponse;

    private HhApi hhApi;

    @BeforeEach
    void setUp() {
        hhApi = new HhApi(httpClient, objectMapper, props);

        when(props.getSearchField()).thenReturn("name");
        when(props.getMinSalary()).thenReturn(100000);
        when(props.getCurrency()).thenReturn("RUR");
        when(props.isOnlyWithSalary()).thenReturn(true);
        when(props.getPerPage()).thenReturn(20);
    }

    @Test
    void fetchVacanciesPageSuccess() throws Exception {
        String expectedJson = "{\"items\":[],\"found\":0,\"pages\":1,\"per_page\":20,\"page\":0}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(expectedJson);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(objectMapper.readTree(expectedJson)).thenReturn(mock(JsonNode.class));

        JsonNode result = hhApi.fetchVacanciesPage(0, "Java", "Москва");

        assertNotNull(result);
        verify(httpClient).send(any(HttpRequest.class), any());
        verify(objectMapper).readTree(expectedJson);
    }

    @Test
    void fetchVacanciesPageErrorResponse() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);

        assertThrows(RuntimeException.class, () ->
                hhApi.fetchVacanciesPage(0, "Java", "Москва")
        );
    }

    @Test
    void fetchVacanciesPageUrlEncoding() throws Exception {
        String expectedJson = "{\"items\":[],\"found\":0,\"pages\":1,\"per_page\":20,\"page\":0}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(expectedJson);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(objectMapper.readTree(expectedJson)).thenReturn(mock(JsonNode.class));

        hhApi.fetchVacanciesPage(0, "Java", "Москва");

        verify(httpClient).send(argThat(request -> {
            String uri = request.uri().toString();
            return uri.contains("text=Java") &&
                    uri.contains("area=1") &&
                    uri.contains("salary_from=100000") &&
                    uri.contains("currency=RUR") &&
                    uri.contains("only_with_salary=true") &&
                    uri.contains("per_page=20");
        }), any());
    }

    @Test
    void fetchVacanciesPageDifferentCity() throws Exception {
        String expectedJson = "{\"items\":[],\"found\":0,\"pages\":1,\"per_page\":20,\"page\":0}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(expectedJson);
        when(httpClient.send(any(HttpRequest.class), any())).thenReturn(httpResponse);
        when(objectMapper.readTree(expectedJson)).thenReturn(mock(JsonNode.class));

        hhApi.fetchVacanciesPage(0, "Java", "Санкт-Петербург");

        verify(httpClient).send(argThat(request -> {
            String uri = request.uri().toString();
            return uri.contains("area=2") &&
                    uri.contains("text=Java") &&
                    uri.contains("search_field=name") &&
                    uri.contains("salary_from=100000") &&
                    uri.contains("currency=RUR") &&
                    uri.contains("only_with_salary=true") &&
                    uri.contains("per_page=20");
        }), any());
    }
}