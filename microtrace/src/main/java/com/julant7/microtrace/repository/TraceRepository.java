package com.julant7.microtrace.repository;

import com.julant7.microtrace.model.Span;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceRepository extends ElasticsearchRepository<Span, String> {
    @Query("SELECT t" +
            "FROM Trace t" +
            "WHERE" +
            "t.traceId = :trace.traceId and" +
            "t.spanId = :trace.spanId and" +
            "t.timestamp = :trace.timestamp and" +
            "t.service = :trace.service and" +
            "t.logLevel = :trace.logLevel and" +
            "t.serviceClass = :traceServiceClass and" +
            "t.message LIKE '%t.message'")
    Span findByTrace(@Param("trace") Span span);
}
