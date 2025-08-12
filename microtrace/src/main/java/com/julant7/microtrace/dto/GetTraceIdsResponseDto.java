package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Setter
@Getter
@Document(indexName = ".ds-traces-generic.otel-default-2025.08.11-000001")
public class GetTraceIdsResponseDto {
    @Field(name = "trace.id", type = FieldType.Keyword)
    private String traceId;
}
