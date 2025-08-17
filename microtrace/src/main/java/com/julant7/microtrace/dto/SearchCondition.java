package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LogicalCondition.class, name = "logical_condition"),
        @JsonSubTypes.Type(value = FieldCondition.class, name = "field_condition")
})
public interface SearchCondition{
}
