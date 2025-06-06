package com.example.service.general;

import com.example.model.Vacancy;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final VacancyService vacancyService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void writeVacanciesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"vacancies.csv\"");

        List<Vacancy> vacancies = vacancyService.getAllVacanciesForStats();

        try (PrintWriter writer = response.getWriter()) {
            // чтобы правильно определил кодировку UTF-8
            writer.write('\uFEFF');

            writer.println("ID,Title,CompanyName,Link,City,Language,SalaryFrom,SalaryTo,Requirement,Responsibility,PublishedAt");

            for (Vacancy v : vacancies) {
                String title       = escapeCsv(v.getTitle());
                String company     = escapeCsv(v.getCompany());
                String link        = escapeCsv(v.getLink());
                String city        = escapeCsv(v.getCity());
                String lang        = escapeCsv(v.getLanguage());
                String salaryFrom  = v.getSalaryFrom() != null ? escapeCsv(v.getSalaryFrom().toString()) : "";
                String salaryTo    = v.getSalaryTo()   != null ? escapeCsv(v.getSalaryTo().toString())   : "";
                String req         = v.getRequirement()     != null ? escapeCsv(v.getRequirement())     : "";
                String resp        = v.getResponsibility()  != null ? escapeCsv(v.getResponsibility())  : "";
                String publishedAt = v.getPublishedAt()     != null ? v.getPublishedAt().format(DTF)   : "";

                String line = String.format(
                        "%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
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
                        publishedAt
                );

                writer.println(line);
            }
            writer.flush();
        }
    }

    public void writeVacanciesXlsx(HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"vacancies.xlsx\"");
        response.setCharacterEncoding("UTF-8");

        List<Vacancy> vacancies = vacancyService.getAllVacanciesForStats();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Vacancies");

            String[] headers = {
                    "ID", "Title", "CompanyName", "Link", "City", "Language",
                    "SalaryFrom", "SalaryTo", "Requirement", "Responsibility", "PublishedAt"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Vacancy v : vacancies) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getTitle() != null ? v.getTitle() : "");
                row.createCell(2).setCellValue(v.getCompany() != null ? v.getCompany() : "");
                row.createCell(3).setCellValue(v.getLink() != null ? v.getLink() : "");
                row.createCell(4).setCellValue(v.getCity() != null ? v.getCity() : "");
                row.createCell(5).setCellValue(v.getLanguage() != null ? v.getLanguage() : "");
                row.createCell(6).setCellValue(v.getSalaryFrom() != null ? v.getSalaryFrom().toString() : "");
                row.createCell(7).setCellValue(v.getSalaryTo()   != null ? v.getSalaryTo().toString()   : "");


                row.createCell(8).setCellValue(v.getRequirement() != null ? v.getRequirement() : "");
                row.createCell(9).setCellValue(v.getResponsibility() != null ? v.getResponsibility() : "");
                row.createCell(10).setCellValue(
                        v.getPublishedAt() != null
                                ? v.getPublishedAt().format(DTF)
                                : ""
                );
            }

            // Автоматическая подгонка ширины колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Запись в поток, только после этого записываем всё пачкой
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                bos.flush();

                byte[] bytes = bos.toByteArray();
                response.setContentLength(bytes.length);

                try (ServletOutputStream out = response.getOutputStream()) {
                    out.write(bytes);
                    out.flush();
                }
            }
        }
    }

    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
