package com.example.service.general;

import com.example.model.Vacancy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExportServiceTest {

    @Mock
    private List<VacancyFetcher> vacancyFetchers;

    private VacancyService vacancyService;
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        vacancyService = Mockito.mock(VacancyService.class);
        exportService = new ExportService(vacancyService);
    }

    @Test
    void testWriteVacanciesCsvSingleVacancyAllFieldsPopulated() throws IOException {
        Vacancy v = new Vacancy();
        v.setId(42L);
        v.setTitle("Java Developer");
        v.setCompany("ExampleCorp");
        v.setLink("https://example.com/job/42");
        v.setCity("Екатеринбург");
        v.setLanguage("Java");
        v.setSalaryFrom(5000);
        v.setSalaryTo(8000);
        v.setRequirement("Must know Spring");
        v.setResponsibility("Implement features");
        v.setPublishedAt(LocalDateTime.of(2025, 1, 15, 10, 30, 0));

        List<Vacancy> list = Collections.singletonList(v);
        when(vacancyService.getAllVacanciesForStats()).thenReturn(list);

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.writeVacanciesCsv(response);

        assertEquals("text/csv; charset=UTF-8", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("vacancies.csv"));

        String raw = response.getContentAsString(StandardCharsets.UTF_8);
        String content = raw.replaceFirst("^\\uFEFF", "");

        String[] lines = content.split("\\r?\\n", -1);
        assertTrue(lines.length >= 2);

        assertEquals(
                "ID,Title,CompanyName,Link,City,Language,SalaryFrom,SalaryTo,Requirement,Responsibility,PublishedAt",
                lines[0]
        );

        String expectedDate = "2025-01-15 10:30:00";
        String expectedDataLine = String.format(
                "42,\"Java Developer\",\"ExampleCorp\",\"https://example.com/job/42\",\"Екатеринбург\",\"Java\",\"5000\",\"8000\",\"Must know Spring\",\"Implement features\",%s",
                expectedDate
        );
        assertEquals(expectedDataLine, lines[1]);
    }

    @Test
    void testWriteVacanciesCsvMultipleVacancies_withNullFields() throws IOException {
        Vacancy full = new Vacancy();
        full.setId(1L);
        full.setTitle("Full Stack");
        full.setCompany("TechCo");
        full.setLink("https://techco.com/job/1");
        full.setCity("Munich");
        full.setLanguage("Go");
        full.setSalaryFrom(3000);
        full.setSalaryTo(6000);
        full.setRequirement("React, Node.js");
        full.setResponsibility("Develop web");
        full.setPublishedAt(LocalDateTime.of(2025, 2, 1, 12, 0, 0));

        Vacancy partial = new Vacancy();
        partial.setId(2L);

        List<Vacancy> list = Arrays.asList(full, partial);
        when(vacancyService.getAllVacanciesForStats()).thenReturn(list);

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.writeVacanciesCsv(response);

        String raw = response.getContentAsString(StandardCharsets.UTF_8);
        String content = raw.replaceFirst("^\\uFEFF", "");
        String[] lines = content.split("\\r?\\n", -1);

        assertTrue(lines.length >= 3);

        assertEquals(
                "ID,Title,CompanyName,Link,City,Language,SalaryFrom,SalaryTo,Requirement,Responsibility,PublishedAt",
                lines[0]
        );

        assertTrue(lines[1].startsWith("1,\"Full Stack\",\"TechCo\",\"https://techco.com/job/1\",\"Munich\",\"Go\",\"3000\",\"6000\",\"React, Node.js\",\"Develop web\",2025-02-01 12:00:00"));

        String[] tokensPartial = lines[2].split(",", -1);
        assertEquals("2", tokensPartial[0]);
        assertEquals(11, tokensPartial.length);
        for (int i = 1; i < tokensPartial.length; i++) {
            assertTrue(
                    tokensPartial[i].equals("") || tokensPartial[i].equals("\"\""),
                    "Ожидается пустое поле, но найдено: [" + tokensPartial[i] + "] на позиции " + i
            );
        }
    }

    @Test
    void testWriteVacanciesXlsxSingleVacancyAndHeader() throws IOException {
        Vacancy v = new Vacancy();
        v.setId(100L);
        v.setTitle("Backend Dev");
        v.setCompany("DataCorp");
        v.setLink("https://datacorp.com/jobs/100");
        v.setCity("Москва");
        v.setLanguage("Java");
        v.setSalaryFrom(7000);
        v.setSalaryTo(9000);
        v.setRequirement("Java, Spring Boot");
        v.setResponsibility("Build REST API");
        v.setPublishedAt(LocalDateTime.of(2025, 3, 10, 9, 45, 0));

        when(vacancyService.getAllVacanciesForStats()).thenReturn(Collections.singletonList(v));

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.writeVacanciesXlsx(response);

        assertTrue(
                response.getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                "Неверный Content-Type: " + response.getContentType()
        );
        assertTrue(response.getHeader("Content-Disposition").contains("vacancies.xlsx"));

        byte[] bytes = response.getContentAsByteArray();
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = workbook.getSheet("Vacancies");
        assertNotNull(sheet, "Лист 'Vacancies' должен существовать");

        Row headerRow = sheet.getRow(0);
        String[] expectedHeaders = {
                "ID", "Title", "CompanyName", "Link", "City", "Language",
                "SalaryFrom", "SalaryTo", "Requirement", "Responsibility", "PublishedAt"
        };
        for (int i = 0; i < expectedHeaders.length; i++) {
            assertEquals(expectedHeaders[i], headerRow.getCell(i).getStringCellValue(),
                    "Заголовок в колонке " + i + " неверен"
            );
        }

        Row dataRow = sheet.getRow(1);
        assertEquals(100L, (long) dataRow.getCell(0).getNumericCellValue());
        assertEquals("Backend Dev", dataRow.getCell(1).getStringCellValue());
        assertEquals("DataCorp", dataRow.getCell(2).getStringCellValue());
        assertEquals("https://datacorp.com/jobs/100", dataRow.getCell(3).getStringCellValue());
        assertEquals("Москва", dataRow.getCell(4).getStringCellValue());
        assertEquals("Java", dataRow.getCell(5).getStringCellValue());
        assertEquals("7000", dataRow.getCell(6).getStringCellValue());
        assertEquals("9000", dataRow.getCell(7).getStringCellValue());
        assertEquals("Java, Spring Boot", dataRow.getCell(8).getStringCellValue());
        assertEquals("Build REST API", dataRow.getCell(9).getStringCellValue());
        assertEquals("2025-03-10 09:45:00", dataRow.getCell(10).getStringCellValue());

        workbook.close();
    }

    @Test
    void testWriteVacanciesXlsxEmptyListGeneratesOnlyHeader() throws IOException {
        when(vacancyService.getAllVacanciesForStats()).thenReturn(Collections.emptyList());

        MockHttpServletResponse response = new MockHttpServletResponse();
        exportService.writeVacanciesXlsx(response);

        byte[] bytes = response.getContentAsByteArray();
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
        Sheet sheet = workbook.getSheet("Vacancies");
        assertNotNull(sheet, "Лист 'Vacancies' должен существовать");

        assertEquals(1, sheet.getPhysicalNumberOfRows());

        Row headerRow = sheet.getRow(0);
        assertNotNull(headerRow);
        assertEquals("ID", headerRow.getCell(0).getStringCellValue());
        assertEquals("Title", headerRow.getCell(1).getStringCellValue());
        assertEquals("CompanyName", headerRow.getCell(2).getStringCellValue());
        assertEquals("PublishedAt", headerRow.getCell(10).getStringCellValue());

        workbook.close();
    }
}