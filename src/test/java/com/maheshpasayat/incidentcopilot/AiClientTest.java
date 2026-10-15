package com.maheshpasayat.incidentcopilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class AiClientTest {

    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String startFake(int status, String body) throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/explain", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    private final Finding finding = new Finding("DEADLOCK", Severity.CRITICAL,
            "Deadlock between 2 threads", List.of("a holds x"));

    @Test
    void success_returnsExplanation() throws Exception {
        String url = startFake(200, "{\"explanation\":\"Lock cycle.\",\"runbook\":\"deadlock\"}");
        AiClient client = new AiClient(new RestTemplateBuilder(), url);

        Explanation e = client.explain(finding).orElseThrow();

        assertEquals("DEADLOCK", e.ruleId());
        assertEquals("Lock cycle.", e.text());
        assertEquals("deadlock", e.runbook());
    }

    @Test
    void serverError_throwsWithoutTheBreakerProxy() throws Exception {
        String url = startFake(500, "{}");
        AiClient client = new AiClient(new RestTemplateBuilder(), url);

        boolean threw = false;
        try {
            client.explain(finding);
        } catch (RuntimeException ex) {
            threw = true;
        }
        assertTrue(threw);
    }
}