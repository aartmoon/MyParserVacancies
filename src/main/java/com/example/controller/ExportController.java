package com.example.controller;

import com.example.service.general.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        exportService.writeVacanciesCsv(response);
    }

    @GetMapping("/export/xlsx")
    public void exportXlsx(HttpServletResponse response) throws IOException {
        exportService.writeVacanciesXlsx(response);
    }
}
