package com.julant7.loglens.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.time.Instant;

@Getter
public class FilterLogRequest {
    @JsonProperty("service")
    private String service;

    @JsonProperty("service_class")
    private String serviceClass;

    @JsonProperty("log_level")
    private String logLevel;

    @JsonProperty("message")
    private String message;



}
