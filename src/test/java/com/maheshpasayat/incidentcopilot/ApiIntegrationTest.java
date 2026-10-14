package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    private String login() throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private MockMultipartFile sample(String folder, String name) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("samples", folder, name));
        return new MockMultipartFile("file", name, "text/plain", bytes);
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void uploadWithoutTokenIsRejected() throws Exception {
        mvc.perform(multipart("/api/analyze/thread-dump").file(sample("thread-dumps", "deadlock.txt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadWithTokenReturnsDeadlockFinding() throws Exception {
        String token = login();
        mvc.perform(multipart("/api/analyze/thread-dump")
                        .file(sample("thread-dumps", "deadlock.txt"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceType").value("THREAD_DUMP"))
                .andExpect(jsonPath("$.findings[0].ruleId").value("DEADLOCK"));
    }

    @Test
    void gcLogUploadReturnsHeapGrowth() throws Exception {
        String token = login();
        mvc.perform(multipart("/api/analyze/gc-log")
                        .file(sample("gc-logs", "memory-leak.log"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.findings[?(@.ruleId=='HEAP_GROWTH')]").exists());
    }

    @Autowired
    ReportRepository reportRepository;

    @Test
    void savedReportCanBeReadBack() throws Exception {
        String token = login();
        mvc.perform(multipart("/api/analyze/thread-dump")
                        .file(sample("thread-dumps", "deadlock.txt"))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Long id = reportRepository.findAll().stream()
                .filter(r -> r.getReportJson().contains("DEADLOCK"))
                .map(ReportEntity::getId)
                .findFirst()
                .orElseThrow();

        String json = mvc.perform(get("/api/reports/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(json.contains("DEADLOCK"));
    }

    @Test
    void emptyFileIsABadRequest() throws Exception {
        String token = login();
        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        mvc.perform(multipart("/api/analyze/thread-dump").file(empty)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}