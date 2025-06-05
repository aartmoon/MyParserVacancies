package com.example.service.trudvsem;

import com.example.model.Vacancy;
import com.example.repository.VacancyRepository;
import com.example.service.general.VacancyLogger;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TrudVsemToVacancy {
    private final VacancyRepository vacancyRepository;
    private final TrudVsemRegionMapper regionMapper;
    private final VacancyLogger logger;

    public Vacancy parseVacancy(JsonObject wrapper) {
        try {
            JsonObject vacObj = wrapper.has("vacancy")
                    ? wrapper.getAsJsonObject("vacancy")
                    : wrapper;

            if (!vacObj.has("job-name") || !vacObj.has("vac_url")) {
                return null;
            }

            String link = vacObj.get("vac_url").getAsString();
            if (vacancyRepository.existsByLink(link)) {
                return null;
            }

            Vacancy vac = new Vacancy();
            vac.setTitle(vacObj.get("job-name").getAsString());
            vac.setLink(link);

            parseCompany(vacObj, vac);
            parseRegion(vacObj, vac);
            parseSalary(vacObj, vac);
            parseDuty(vacObj, vac);
            parseRequirements(vacObj, vac);
            parseCreationDate(vacObj, vac);

            return vac;
        } catch (Exception e) {
            logger.logFailedToSave("unknown", e.getMessage());
            return null;
        }
    }

    private void parseCompany(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("company") && vacObj.get("company").isJsonObject()) {
            JsonObject comp = vacObj.getAsJsonObject("company");
            if (comp.has("name")) {
                vac.setCompany(comp.get("name").getAsString());
            }
        }
    }

    private void parseRegion(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("region") && vacObj.get("region").isJsonObject()) {
            JsonObject region = vacObj.getAsJsonObject("region");
            if (region.has("name")) {
                String regionName = region.get("name").getAsString();
                String city = regionMapper.extractCityFromRegion(regionName);
                if (city != null) {
                    vac.setCity(city);
                }
            }
        }
    }

    private void parseSalary(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("salary_min") || vacObj.has("salary_max")) {
            Integer salaryFrom = null;
            Integer salaryTo = null;

            if (vacObj.has("salary_min")) {
                int minSalary = vacObj.get("salary_min").getAsInt();
                if (minSalary > 0) {
                    salaryFrom = minSalary;
                }
            }
            if (vacObj.has("salary_max")) {
                int maxSalary = vacObj.get("salary_max").getAsInt();
                if (maxSalary > 0) {
                    salaryTo = maxSalary;
                }
            }

            if (salaryFrom != null || salaryTo != null) {
                vac.setSalaryFrom(salaryFrom);
                vac.setSalaryTo(salaryTo);
                vac.setCurrency("RUB");
            }
        }
    }

    private void parseDuty(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("duty")) {
            vac.setResponsibility(vacObj.get("duty").getAsString());
        }
    }

    private void parseRequirements(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("requirement") && vacObj.get("requirement").isJsonObject()) {
            JsonObject req = vacObj.getAsJsonObject("requirement");
            StringBuilder requirements = new StringBuilder();

            if (req.has("education")) {
                requirements.append("Образование: ").append(req.get("education").getAsString()).append("\n");
            }
            if (req.has("experience")) {
                requirements.append("Опыт работы: ").append(req.get("experience").getAsInt()).append(" лет\n");
            }

            vac.setRequirement(requirements.toString().trim());
        }
    }

    private void parseCreationDate(JsonObject vacObj, Vacancy vac) {
        if (vacObj.has("creation-date")) {
            String dateStr = vacObj.get("creation-date").getAsString();
            try {
                LocalDate date = LocalDate.parse(dateStr);
                vac.setPublishedAt(date.atStartOfDay());
            } catch (Exception ex) {
                logger.logDateParseError(dateStr, ex);
            }
        }
    }
}