package com.julant7.microtrace.service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.json.JsonData;
import com.julant7.microtrace.dto.GetSpansInTraceRequestDto;
import com.julant7.microtrace.dto.GetTraceByFilterRequest;
import com.julant7.microtrace.dto.OperationOperator;
import com.julant7.microtrace.dto.ServiceOperator;
import com.julant7.microtrace.model.Span;
import com.julant7.microtrace.dto.GetTraceResponseDto;
import com.julant7.microtrace.dto.ServiceBucket;
import com.julant7.microtrace.dto.TraceBucket;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.julant7.microtrace.dto.TimeInterval.MINUTES_5;

@AllArgsConstructor
@Service
public class TraceSearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public GetTraceResponseDto findTracesByFilters(GetTraceByFilterRequest request) {
        List<FieldValue> traceIds = getTraceIdsByFilters(request);
        SearchHits<Span> searchHits = getGraphs(traceIds, request);
        return toDto(searchHits);
    }

    private List<FieldValue> getTraceIdsByFilters(GetTraceByFilterRequest request) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        queryBuilder.withFields("trace.id");
        System.out.println(request.getTimeInterval() != null);

        if (request.getTimeInterval() != null) {
            System.out.println(request.getTimeInterval() == MINUTES_5);
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            queryBuilder.withFilter(q -> q
                    .range(r -> r
                            .date(v -> {
                                v.field("@timestamp")
                                        .lte(formatter.format(now));
                                switch (request.getTimeInterval()) {
                                    case MINUTES_5  -> v.gte(formatter.format(now.minus(5, ChronoUnit.MINUTES)));
                                    case MINUTES_15 -> v.gte(formatter.format(now.minus(15, ChronoUnit.MINUTES)));
                                    case HOURS_1 -> v.gte(formatter.format(now.minus(1, ChronoUnit.HOURS)));
                                    case HOURS_3 -> v.gte(formatter.format(now.minus(3, ChronoUnit.HOURS)));
                                    case HOURS_6 -> v.gte(formatter.format(now.minus(6, ChronoUnit.HOURS)));
                                    case HOURS_12 -> v.gte(formatter.format(now.minus(12, ChronoUnit.HOURS)));
                                    case DAY_1 -> v.gte(formatter.format(now.minus(1, ChronoUnit.DAYS)));
                                    case DAYS_3 -> v.gte(formatter.format(now.minus(3, ChronoUnit.DAYS)));
                                }
                                return v;
                            })));
        }
        if (request.getServices() != null) {
            List<FieldValue> fieldValues = new ArrayList<>();
            request.getServices().forEach(f -> fieldValues.add(FieldValue.of(f)));
            if (request.getServiceOperator() == ServiceOperator.OR) {
                queryBuilder.withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .terms(t -> t
                                                .field("service")
                                                .terms(te -> te
                                                        .value(fieldValues))))));
            }
        }
        if (request.getOperation() != null) {
            List<FieldValue> fieldValues = new ArrayList<>();
            request.getOperation().forEach(f -> fieldValues.add(FieldValue.of(f)));
            if (request.getOperationOperator() == OperationOperator.OR) {
                queryBuilder.withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .terms(t -> t
                                                .field("service")
                                                .terms(te -> te
                                                        .value(fieldValues))))));
            }
        }
        NativeQuery query = queryBuilder.build();
        SearchHits<Span> searchHits = elasticsearchOperations.search(query, Span.class);

        System.out.println(searchHits.stream()
                .map(SearchHit::getContent)
                .map(Span::getTraceId)
                .distinct()
                .toList());
        return searchHits.stream()
                .map(SearchHit::getContent)
                .map(Span::getTraceId)
                .map(FieldValue::of)
                .distinct()
                .toList();
    }

    private SearchHits<Span> getGraphs(List<FieldValue> traceIds, GetTraceByFilterRequest request) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        // filter inside aggregation
        queryBuilder.withQuery(q -> q
                .bool(b -> b
                        .must(m -> m
                                .terms(ta -> ta
                                        .field("trace.id")
                                        .terms(te -> te
                                                .value(traceIds))))));

        Aggregation aggByService = Aggregation.of(a -> a
                .terms(ta -> ta.field("service.name"))
                .aggregations("sumDurationByService", Aggregation.of(sa -> sa.sum(s -> s.field("duration")))));

        Aggregation aggByTrace = Aggregation.of(a -> a
                .terms(ta -> ta.field("trace.id"))
                .aggregations("sumDurationByTrace", Aggregation.of(sa -> sa.sum(s -> s.field("duration"))))
                .aggregations("minTimestamp", Aggregation.of(sa -> sa.min(m -> m.field("@timestamp"))))
                .aggregations("startName", Aggregation.of(th -> th
                        .topHits(h -> h
                                .size(1)
                                .sort(s -> s.field(f -> f.field("@timestamp").order(SortOrder.Asc)))
                                .source(fn -> fn.filter(f -> f.includes("name")))
                        )))
                .aggregations("byService", aggByService)
                .aggregations("filterByTraceDuration", agg -> agg
                        .bucketSelector(bs -> bs
                                .bucketsPath(bp -> bp
                                        .dict(Map.of("calculatedDuration", "sumDurationByTrace.value")))
                                .script(sc -> {
                                    StringBuilder scriptSource = new StringBuilder();
                                    if (request.getMaxDuration() != null) {
                                        scriptSource.append("params.calculatedDuration <= ").append(request.getMaxDuration());
                                    }
                                    if (request.getMinDuration() != null) {
                                        if (request.getMaxDuration() != null) scriptSource.append(" && ");
                                        scriptSource.append("params.calculatedDuration >= ").append(request.getMinDuration());
                                    }
                                    sc.source(scriptSource.isEmpty() ? "true" : scriptSource.toString());
                                    return sc;
                                })
                        )));
        queryBuilder.withAggregation("byTrace", aggByTrace);
        NativeQuery query = queryBuilder.build();
        SearchHits<Span> searchHits = elasticsearchOperations.search(query, Span.class);
        return searchHits;
    }

    private GetTraceResponseDto toDto(SearchHits<Span> searchHits) {
        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
        assert aggregations != null;
        List<StringTermsBucket> traces = aggregations
                .aggregationsAsMap()
                .get("byTrace")
                .aggregation()
                .getAggregate()
                .sterms()
                .buckets()
                .array();
        List<TraceBucket> result = new ArrayList<>();
        traces.forEach(traceTermsBucket -> {
            List<StringTermsBucket> services = traceTermsBucket.aggregations().get("byService").sterms().buckets().array();
            List<ServiceBucket> serviceBuckets = new ArrayList<>();
            services.forEach(serviceTermsBucket -> {
                serviceBuckets.add(new ServiceBucket(
                        serviceTermsBucket.key().stringValue(),
                        serviceTermsBucket.aggregations().get("sumDurationByService").sum().value(),
                        serviceTermsBucket.docCount()
                        ))
                ;
            });
            JsonData jsonData = (JsonData) traceTermsBucket.aggregations().get("startName").topHits().hits().hits().get(0).source();
            // jsonData.
            result.add(new TraceBucket(
                    traceTermsBucket.key().stringValue(),
                    traceTermsBucket.aggregations().get("sumDurationByTrace").sum().value(),
                    traceTermsBucket.aggregations().get("startName").topHits().hits().hits().get(0).source().toJson().asJsonObject().getString("name"),
                    traceTermsBucket.aggregations().get("minTimestamp").min().valueAsString(),
                    serviceBuckets
                    )
            );
        });
        return new GetTraceResponseDto(result);
    }

    public List<Span> getSpansInTrace(GetSpansInTraceRequestDto getSpansInTraceRequestDto) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        FieldSort fieldSort = FieldSort.of(f -> f.field("@timestamp"));
        queryBuilder.withFilter(f -> f
                .match(t -> t
                        .field("trace.id")
                        .query(getSpansInTraceRequestDto.getTraceId())));
        queryBuilder.withSort(q -> q
                .field(fieldSort));
        NativeQuery query = queryBuilder.build();
        SearchHits<Span> spans = elasticsearchOperations.search(query, Span.class);
        return spans.stream()
                .map(SearchHit::getContent)
                .toList();
    }

}
