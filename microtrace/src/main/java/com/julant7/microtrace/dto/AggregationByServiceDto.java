package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregationByServiceDto {
    @JsonProperty("key")
    private String service;
    @JsonProperty("sumDurationByService.value")
//    @JsonProperty("sumDurationByService")
    private Double sumDurationByService;
    @JsonProperty("countInService.value")
//    @JsonProperty("countInService")
    private String countInService;
}
