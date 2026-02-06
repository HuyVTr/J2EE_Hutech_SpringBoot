package fit.hutech.spring.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.services.ReportService;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String index() {
        return "admin/reports";
    }

    @GetMapping("/export/top-selling")
    public ResponseEntity<InputStreamResource> exportTopSelling() throws IOException {
        ByteArrayInputStream in = reportService.exportTopSellingBooks();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=top-selling-books.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @org.springframework.web.bind.annotation.PostMapping("/export")
    public ResponseEntity<InputStreamResource> exportReport(
            @org.springframework.web.bind.annotation.ModelAttribute fit.hutech.spring.dtos.ReportRequest request)
            throws IOException {
        ByteArrayInputStream in = reportService.generateReport(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=report_" + System.currentTimeMillis() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
