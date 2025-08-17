package com.julant7.microtrace.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Setter
@Getter
@Document(indexName = ".ds-traces-generic.otel-default-*")
public class Span {
    @Id
    @Field(name = "_id")
    private String id;

    @Field(name = "trace.id")
    private String traceId;

    @Field(name = "parent_span_id")
    private String parentSpanId;

    @Field(name = "span.id")
    private String spanId;

    @Field(name = "@timestamp")
    private String timestamp;

    @Field(name = "duration")
    private Long duration;

    @Field(name = "service.name")
    private String service;

    @Field(name = "scope.name")
    private String serviceClass;


}
