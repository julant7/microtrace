package com.julant7.client.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MdcFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        Tracer tracer = OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder().build()).build()
                .getTracer("oTelTracer");
        Span restSpan = tracer.spanBuilder("restSpan").setParent(Context.root()).startSpan();
        try {
            MDC.put("trace_id", restSpan.getSpanContext().getTraceId());
            MDC.put("span_id", restSpan.getSpanContext().getSpanId());
            MDC.put("trace_flags", String.valueOf(restSpan.getSpanContext().getTraceFlags()));
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}