package com.maheshpasayat.incidentcopilot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService service;
    private final ReportRepository repository;
    private final ObjectMapper objectMapper;

    public AnalysisController(AnalysisService service, ReportRepository repository, ObjectMapper objectMapper) {
        this.service = service;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/analyze/thread-dump", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisReport> threadDump(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AnalysisReport report = service.analyzeThreadDump(read(file));
        save(report);
        return ResponseEntity.ok(report);
    }

    @PostMapping(value = "/analyze/gc-log", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisReport> gcLog(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AnalysisReport report = service.analyzeGcLog(read(file));
        save(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<String> report(@PathVariable Long id) {
        return repository.findById(id)
                .map(r -> ResponseEntity.ok().header("Content-Type", "application/json").body(r.getReportJson()))
                .orElse(ResponseEntity.notFound().build());
    }

    private String read(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private void save(AnalysisReport report) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(report);
        repository.save(new ReportEntity(
                report.sourceType(), report.factsCount(), report.findings().size(), json));
    }
}