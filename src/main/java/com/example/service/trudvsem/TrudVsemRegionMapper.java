package com.example.service.trudvsem;

import com.example.service.general.VacancyLogger;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import static com.example.config.Constants.CITY_TO_REGION_CODE_TRUDVSEM;

@Component
@RequiredArgsConstructor
public class TrudVsemRegionMapper {

    private final VacancyLogger logger;

    public String getRegionCode(String city) {
        String code = CITY_TO_REGION_CODE_TRUDVSEM.get(city);
        if (code == null) {
            logger.logFailedToSave(city, "Region code not found");
        }
        return code;
    }

    public String extractCityFromRegion(String regionName) {
        String city = regionName.replaceFirst("^Город\\s+", "");

        for (String knownCity : CITY_TO_REGION_CODE_TRUDVSEM.keySet()) {
            if (city.contains(knownCity)) {
                return knownCity;
            }
        }
        logger.logFailedToSave(regionName, "City not found in region name");
        return null;
    }
}