package com.julant7.microtrace.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Обобщенные данные по сервисам в трейсе")
public record ServiceBucket(
        @Schema(description = "Название сервиса", example = "order-service")
        String service,
        @Schema(description = "Длительность всех спанов в сервисе", example = "order-service")
        Double sumDurationByService,
        @Schema(description = "Количество спанов в сервисе", example = "5")
        Long countInService
) {}