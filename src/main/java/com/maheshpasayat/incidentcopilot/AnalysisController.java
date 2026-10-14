package com.maheshpasayat.incidentcopilot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analyze")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/thread-dump")
    public ResponseEntity<AnalysisReport> threadDump(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(service.analyzeThreadDump(text));
    }

    @PostMapping("/gc-log")
    public ResponseEntity<AnalysisReport> gcLog(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(service.analyzeGcLog(text));
    }
}