package com.julant7.client.dto;

import lombok.Getter;

@Getter
public class InsertLogRequestDto {
    private String serviceName;
    private LogLevel logLevel;
    private String message;
}
