package com.julant7.microtrace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Schema(description = "Список фильтров трейсов")
@Getter
public class GetTraceByFilterRequest {
    @Schema(description = "Запрос c фильтрами для трейса", example = "(service:ServiceA OR service:ServiceB) AND duration:>100ms AND status:ERROR")
    @JsonProperty("query")
    private String query;

    @Schema(description = "Список сервисов, участвующих в трейсе", example = "[\"order-service\", \"user-service\"]", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("services")
    private List<String> services;

    @Schema(description = "Логический оператор связи сервисов", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("service_operator")
    private ServiceOperator serviceOperator;

    @Schema(description = "Начальная операция трейса", example = "POST /user", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("operation")
    private List<String> operation;

    @Schema(description = "Логический оператор связи операций", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("operation_operator")
    private OperationOperator operationOperator;

    @Schema(description = "Временной интервал поиска", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("time_interval")
    private TimeInterval timeInterval;

    @Schema(description = "Минимальная длительность трейса в мс", example = "1000000", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("min_duration")
    private String minDuration;

    @Schema(description = "Максимальная длительность трейса в мс", example = "2000000", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("max_duration")
    private String maxDuration;

}
