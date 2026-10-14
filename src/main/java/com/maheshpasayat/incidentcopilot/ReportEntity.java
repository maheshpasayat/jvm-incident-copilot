package com.maheshpasayat.incidentcopilot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private int factsCount;

    @Column(nullable = false)
    private int findingsCount;

    @Column(nullable = false, columnDefinition = "text")
    private String reportJson;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReportEntity() {
    }

    public ReportEntity(String sourceType, int factsCount, int findingsCount, String reportJson) {
        this.sourceType = sourceType;
        this.factsCount = factsCount;
        this.findingsCount = findingsCount;
        this.reportJson = reportJson;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public int getFactsCount() { return factsCount; }
    public int getFindingsCount() { return findingsCount; }
    public String getReportJson() { return reportJson; }
    public Instant getCreatedAt() { return createdAt; }
}