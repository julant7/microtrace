package com.julant7.microtrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Обобщенные данных трейсов по фильтрам")
public record TraceBucket(
        @Schema(description = "Id трейса", example = "2cd92dd148ba46713cf3afd36502997c")
        String traceId,
        @Schema(description = "Длительность трейса", example = "10020020")
        double sumDurationByTrace,
        @Schema(description = "Название начальной операции трейса", example = "POST /order")
        String startName,
        @Schema(description = "Время начала", example = "2025-08-12T16:22:18.197Z")
        String minTimestamp,
        @Schema(description = "Список обобщенных данных по сервисам трейса")
        List<ServiceBucket> byService
) {}