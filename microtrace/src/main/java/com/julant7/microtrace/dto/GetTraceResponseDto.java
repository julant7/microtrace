package com.julant7.microtrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Schema(description = "Список обобщенных данных трейсов по фильтрам")
public record GetTraceResponseDto(
        @Schema(description = "Список трейсов")
        List<TraceBucket> traces
) {}
