package com.julant7.microtrace.controller;

import com.julant7.microtrace.dto.GetSpansInTraceRequestDto;
import com.julant7.microtrace.dto.GetTraceByFilterRequest;
import com.julant7.microtrace.dto.GetTraceResponseDto;
import com.julant7.microtrace.dto.TraceSearchRequestDto;
import com.julant7.microtrace.model.Span;
import com.julant7.microtrace.service.TraceSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@Tag(name = "TraceController", description = "Контроллер для получения трейсов и спанов")
public class TraceController {
    TraceSearchService traceSearchService;
    @GetMapping("/get_traces_by_filters")
    @Operation(
           summary = "Получение трейсов по фильтрам"
    )
    ResponseEntity<GetTraceResponseDto> getTracesByFilters(
            @RequestBody @Parameter(description = "DTO", required = true, example = "") TraceSearchRequestDto requestDto) {
        return ResponseEntity.ok(traceSearchService.findTracesByFilters(requestDto));
    }

    @GetMapping("/get_spans_in_trace")
    @Operation(
            summary = "Получение спанов по traceId"
    )
    ResponseEntity<List<Span>> getSpansInTrace(@RequestBody GetSpansInTraceRequestDto getSpansInTraceRequestDto) {
        return ResponseEntity.ok(traceSearchService.getSpansInTrace(getSpansInTraceRequestDto));
    }

}
