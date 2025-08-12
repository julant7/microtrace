package com.julant7.microtrace.dto;

public record ServiceBucket(
        String service,
        Double sumDurationByService,
        Double countInService
) {}