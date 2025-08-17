package com.julant7.microtrace.dto;

public record FieldCondition(
        Field field,
        FieldOperator operator,
        Object value
) implements SearchCondition { }
