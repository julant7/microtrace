package com.julant7.microtrace.dto;

import java.util.List;

public record TraceBucket(
        String traceId,
        double sumDurationByTrace,
        String startName,
        Double minTimestamp,
        List<ServiceBucket> byService
) {}