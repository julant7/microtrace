package com.julant7.microtrace.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Data
@Document(indexName = ".ds-traces-generic.otel-default-2025.08.04-000001")
@NoArgsConstructor
public class Span {
    @Id
    private String id;

    private String traceId;

    private String spanId;

    private Instant timestamp;

    private Instant duration;

    private String service;

    @Column(name = "scope.name")
    private String serviceClass;

    private String message;

    @Builder
    public Span(String service, String message, Instant timestamp) {
        this.service = service;
        this.message = message;
        this.timestamp = timestamp;
    }
}
