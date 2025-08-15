package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;

@Builder
public class GetSpansInTraceResponseDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("parent_span_id")
    private String parentSpanId;

    @JsonProperty("span_id")
    private String spanId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("service")
    private String service;

    @JsonProperty("trace_id")
    private String serviceClass;

}
