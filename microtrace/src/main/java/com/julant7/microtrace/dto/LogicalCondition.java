package com.julant7.microtrace.dto;

import java.util.List;

public record LogicalCondition(
        List<SearchCondition> conditions,
        LogicalOperator operator
) implements SearchCondition {
}
