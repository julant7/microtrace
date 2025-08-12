package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class AggregationByTraceDto {
    @JsonProperty("key")
    private String traceId;
    @JsonProperty("sumDurationByTrace.value")
//    @JsonProperty("sumDurationByTrace")
    private double sumDurationByTrace;
    @JsonProperty("startName.hits.hits._source.name")
//    @JsonProperty("startName")
    private String startName;
    @JsonProperty("minTimestamp.value")
//    @JsonProperty("minTimestamp")
    private Instant minTimestamp;
    @JsonProperty("byService.buckets")
//    @JsonProperty("byService")
    private List<AggregationByServiceDto> byService;
}
