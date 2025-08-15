package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Id трейса")
@Getter
public class GetSpansInTraceRequestDto {
    @Schema(description = "Id трейса", example = "2cd92dd148ba46713cf3afd36502997c", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("trace_id")
    private String traceId;
}
