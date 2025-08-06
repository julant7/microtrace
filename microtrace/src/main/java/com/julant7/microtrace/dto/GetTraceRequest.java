package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GetTraceRequest {
    @JsonProperty("service")
    private String service;

    @JsonProperty("service_class")
    private String serviceClass;

    @JsonProperty("time_interval")
    private TimeIntervals timeIntervals;

    @JsonProperty("message")
    private String message;

    @JsonProperty("min_duration")
    private String minDuration;

    @JsonProperty("max_duration")
    private String maxDuration;

}
