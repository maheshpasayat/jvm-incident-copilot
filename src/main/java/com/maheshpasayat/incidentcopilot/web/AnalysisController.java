package com.maheshpasayat.incidentcopilot.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.maheshpasayat.incidentcopilot.ReportEntity;
import com.maheshpasayat.incidentcopilot.ReportRepository;
import com.maheshpasayat.incidentcopilot.core.AnalysisReport;
import com.maheshpasayat.incidentcopilot.core.Explanation;
import com.maheshpasayat.incidentcopilot.service.AiClient;
import com.maheshpasayat.incidentcopilot.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.instrument.MeterRegistry;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService service;
    private final ReportRepository repository;
    private final ObjectMapper objectMapper;
    private final AiClient aiClient;
    private final MeterRegistry meters;

    public AnalysisController(AnalysisService service, ReportRepository repository, ObjectMapper objectMapper, AiClient aiClient, MeterRegistry meters) {
        this.service = service;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.aiClient = aiClient;
        this.meters = meters;
    }

    @PostMapping(value = "/analyze/thread-dump", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisReport> threadDump(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AnalysisReport report = enrich(service.analyzeThreadDump(read(file)));
        save(report);
        return ResponseEntity.ok(report);
    }

    @PostMapping(value = "/analyze/gc-log", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisReport> gcLog(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AnalysisReport report = enrich(service.analyzeGcLog(read(file)));
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

    private AnalysisReport enrich(AnalysisReport report) {
        meters.counter("copilot.analyses", "source", report.sourceType()).increment();
        report.findings().forEach(f ->
                meters.counter("copilot.findings", "rule", f.ruleId(), "severity", f.severity().name()).increment());

        if (report.findings().isEmpty()) {
            return report;
        }
        List<Explanation> explanations = report.findings().stream()
                .map(aiClient::explain)
                .flatMap(java.util.Optional::stream)
                .toList();
        meters.counter("copilot.ai.explanations", "available", String.valueOf(!explanations.isEmpty())).increment();
        return explanations.isEmpty() ? report : report.withExplanations(explanations);
    }
}