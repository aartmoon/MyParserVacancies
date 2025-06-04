package com.example.controller;

import com.example.model.Vacancy;
import com.example.service.general.VacancyService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExportController {

    private final VacancyService vacancyService;

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"vacancies.csv\"");

        List<Vacancy> vacancies = vacancyService.getVacancies(null, null, false);

        PrintWriter writer = response.getWriter();
        // задаём кодировку UTF-8
        writer.write('\uFEFF');
        writer.println("ID,Title,CompanyName,Link,City,Language,SalaryFrom,SalaryTo,Requirement,Responsibility,PublishedAt");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Vacancy v : vacancies) {
            String title = escapeCsv(v.getTitle());
            String company = escapeCsv(v.getCompany());
            String link = escapeCsv(v.getLink());
            String city = escapeCsv(v.getCity());
            String lang = escapeCsv(v.getLanguage());
            String salaryFrom = (v.getSalaryFrom() != null) ? escapeCsv(v.getSalaryFrom().toString()) : "";
            String salaryTo = (v.getSalaryTo() != null) ? escapeCsv(v.getSalaryTo().toString()) : "";
            String req = (v.getRequirement() != null) ? escapeCsv(v.getRequirement()) : "";
            String resp = (v.getResponsibility() != null) ? escapeCsv(v.getResponsibility()) : "";
            String published = (v.getPublishedAt() != null) ? v.getPublishedAt().format(dtf) : "";

            String line = String.format(
                    "%d,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    v.getId(),
                    title,
                    company,
                    link,
                    city,
                    lang,
                    salaryFrom,
                    salaryTo,
                    req,
                    resp,
                    published
            );
            writer.println(line);
        }
        writer.flush();
    }

    private String escapeCsv(String field) {
        if (field == null) return "";
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    @GetMapping("/export/xlsx")
    public void exportXlsx(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"vacancies.xlsx\"");
        response.setCharacterEncoding("UTF-8");

        List<Vacancy> vacancies = vacancyService.getVacancies(null, null, false);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Vacancies");

            String[] headers = {
                    "ID",
                    "Title",
                    "CompanyName",
                    "Link",
                    "City",
                    "Language",
                    "SalaryFrom",
                    "SalaryTo",
                    "Requirement",
                    "Responsibility",
                    "PublishedAt"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            int rowIdx = 1;
            for (Vacancy v : vacancies) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getTitle() != null ? v.getTitle() : "");
                row.createCell(2).setCellValue(v.getCompany() != null ? v.getCompany() : "");
                row.createCell(3).setCellValue(v.getLink() != null ? v.getLink() : "");
                row.createCell(4).setCellValue(v.getCity() != null ? v.getCity() : "");
                row.createCell(5).setCellValue(v.getLanguage() != null ? v.getLanguage() : "");

                if (v.getSalaryFrom() != null) {
                    row.createCell(6).setCellValue(v.getSalaryFrom().toString());
                } else {
                    row.createCell(6).setCellValue("");
                }

                if (v.getSalaryTo() != null) {
                    row.createCell(7).setCellValue(v.getSalaryTo().toString());
                } else {
                    row.createCell(7).setCellValue("");
                }

                row.createCell(8).setCellValue(v.getRequirement() != null ? v.getRequirement() : "");
                row.createCell(9).setCellValue(v.getResponsibility() != null ? v.getResponsibility() : "");
                row.createCell(10).setCellValue(
                        v.getPublishedAt() != null
                                ? v.getPublishedAt().format(dtf)
                                : ""
                );
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                bos.flush();

                byte[] bytes = bos.toByteArray();
                response.setContentLength(bytes.length);

                ServletOutputStream out = response.getOutputStream();
                out.write(bytes);
                out.flush();
            }
        }
    }
}
