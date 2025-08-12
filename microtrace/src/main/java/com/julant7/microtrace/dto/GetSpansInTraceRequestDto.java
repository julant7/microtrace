package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GetSpansInTraceRequestDto {
    @JsonProperty("trace_id")
    private String traceId;
}
