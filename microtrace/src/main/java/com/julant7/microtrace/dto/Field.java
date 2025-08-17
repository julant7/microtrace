package com.julant7.microtrace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Field {
    SERVICE("service.name", FieldType.KEYWORD),
    OPERATION("name", FieldType.KEYWORD),
    TIMESTAMP("@timestamp", FieldType.DATE),
    DURATION("duration", FieldType.LONG),
    HTTP_STATUS_CODE("http.status_code", FieldType.KEYWORD),
    STATUS_CODE("status.code", FieldType.KEYWORD);
    private final String elasticsearchFieldName;
    private final FieldType fieldType;
    public enum FieldType{
        KEYWORD,
        TEXT,
        DATE,
        LONG,
        DOUBLE,
        BOOLEAN
    }
}
