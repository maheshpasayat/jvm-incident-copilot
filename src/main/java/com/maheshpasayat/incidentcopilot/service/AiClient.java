package com.maheshpasayat.incidentcopilot.service;

import com.maheshpasayat.incidentcopilot.core.Explanation;
import com.maheshpasayat.incidentcopilot.core.Finding;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@Component
public class AiClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public AiClient(@Value("${app.ai.base-url}") String baseUrl) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        this.rest = new RestTemplate(factory);
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "unavailable")
    public Optional<Explanation> explain(Finding finding) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of("title", finding.title(), "evidence", finding.evidence());

        Map<?, ?> body = rest.postForObject(baseUrl + "/explain",
                new HttpEntity<>(payload, headers), Map.class);
        if (body == null) {
            throw new IllegalStateException("empty response from AI service");
        }
        return Optional.of(new Explanation(finding.ruleId(),
                String.valueOf(body.get("explanation")), String.valueOf(body.get("runbook"))));
    }

    @SuppressWarnings("unused")
    private Optional<Explanation> unavailable(Finding finding, Throwable cause) {
        org.slf4j.LoggerFactory.getLogger(AiClient.class)
                .warn("AI explain failed for {}: {}", finding.ruleId(), cause.toString());
        return Optional.empty();
    }
}