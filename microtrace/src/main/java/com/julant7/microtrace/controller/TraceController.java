package com.julant7.microtrace.controller;

import com.julant7.microtrace.dto.GetSpansInTraceRequestDto;
import com.julant7.microtrace.dto.GetSpansResponseDto;
import com.julant7.microtrace.dto.GetTraceByFilterRequest;
import com.julant7.microtrace.dto.GetTraceResponseDto;
import com.julant7.microtrace.model.Span;
import com.julant7.microtrace.service.TraceSearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class TraceController {
    TraceSearchService traceSearchService;
    @GetMapping("/get_traces_by_filters")
    ResponseEntity<GetTraceResponseDto> getTracesByFilters(@RequestBody GetTraceByFilterRequest getTraceByFilterRequest) {
        return ResponseEntity.ok(traceSearchService.findTracesByFilters(getTraceByFilterRequest));
    }

    @GetMapping("/get_spans_in_trace")
    ResponseEntity<List<Span>> getSpansInTrace(@RequestBody GetSpansInTraceRequestDto getSpansInTraceRequestDto) {
        return ResponseEntity.ok(traceSearchService.getSpansInTrace(getSpansInTraceRequestDto));
    }
}
