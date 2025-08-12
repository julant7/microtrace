package com.julant7.microtrace.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.Instant;

@Data
@Document(indexName = ".ds-traces-generic.otel-default-2025.08.11-000001")
@NoArgsConstructor
public class Span {
    @Id
    @Field(name = "_id")
    private String id;

    @Field(name = "trace.id")
    private String traceId;

    @Field(name = "span.id")
    private String spanId;

    @Field(name = "@timestamp")
    private String timestamp;

    @Field(name = "duration")
    private Integer duration;

    @Field(name = "service")
    private String service;

    @Field(name = "scope.name")
    private String serviceClass;

    // @Field(name = "span.id")
    private String message;

    @Builder
    public Span(String service, String message, String timestamp) {
        this.service = service;
        this.message = message;
        this.timestamp = timestamp;
    }
}
