package com.julant7.microtrace.model;

import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = ".ds-logs-generic.otel-default-*")
public class Log {
}
