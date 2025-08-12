package com.julant7.microtrace.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Document(indexName = ".ds-traces-generic.otel-default-2025.08.11-000001")
public record GetTraceResponseDto(
        List<TraceBucket> traces
) {}
