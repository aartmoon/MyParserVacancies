package com.example.service.trudvsem;

import com.example.service.general.VacancyLogger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

import static com.example.config.Constants.PAGE_SIZE_TRUDVSEM;

@Component
@RequiredArgsConstructor
public class TrudVsemApi {

    private static final String API_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";

    private final RestTemplate restTemplate;
    private final TrudVsemRegionMapper regionMapper;
    private final VacancyLogger logger;

    public JsonObject fetchVacanciesPage(String language, String city, int page) {
        if (language == null || language.isBlank()) {
            return null;
        }

        String searchQuery = String.format("%s", language);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("offset", page * PAGE_SIZE_TRUDVSEM)
                .queryParam("limit", PAGE_SIZE_TRUDVSEM)
                .queryParam("text", searchQuery);

        if (city != null && !city.isBlank()) {
            String regionCode = regionMapper.getRegionCode(city);
            if (regionCode != null) {
                builder.queryParam("region_code", regionCode);
            }
        }

        URI uri = builder.build().encode().toUri();
        logger.logFetchingPage(page);

        try {
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                logger.logNoMoreItems(page);
                return null;
            }
            return JsonParser.parseString(jsonResponse).getAsJsonObject();
        } catch (Exception e) {
            logger.logFailedToSave(uri.toString(), e.getMessage());
            return null;
        }
    }
}