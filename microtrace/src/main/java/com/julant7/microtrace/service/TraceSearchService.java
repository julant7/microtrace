package com.julant7.microtrace.service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.julant7.microtrace.dto.GetSpansInTraceRequestDto;
import com.julant7.microtrace.dto.GetSpansResponseDto;
import com.julant7.microtrace.dto.GetTraceByFilterRequest;
import com.julant7.microtrace.dto.GetTraceIdsResponseDto;
import com.julant7.microtrace.dto.GetTraceResponseDto;
import com.julant7.microtrace.dto.ServiceBucket;
import com.julant7.microtrace.dto.TraceBucket;
import com.julant7.microtrace.model.Span;
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

@AllArgsConstructor
@Service
public class TraceSearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public GetTraceResponseDto findTracesByFilters(GetTraceByFilterRequest request) {
        List<FieldValue> traceIds = getTraceIdsByFilters(request);
        SearchHits<GetTraceIdsResponseDto> searchHits = getGraphs(traceIds);
        return toDto(searchHits);
    }

    private List<FieldValue> getTraceIdsByFilters(GetTraceByFilterRequest request) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        queryBuilder.withFields("trace.id");
        if (request.getTimeIntervals() != null) {
            Instant now = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            queryBuilder.withFilter(q -> q
                    .range(r -> r
                            .date(v -> {
                                v.field("@timestamp")
                                        .lte(formatter.format(now));
                                switch (request.getTimeIntervals()) {
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
        if (request.getService() != null) {
            queryBuilder.withFilter(q -> q
                    .match(t -> t
                            .field("service")
                            .query(request.getService())));
        }
        if (request.getServiceClass() != null) {
            queryBuilder.withFilter(q -> q
                    .match(t -> t
                            .field("scope.name")
                            .query(request.getServiceClass())));
        }
        if (request.getMessage() != null) {
            queryBuilder.withFilter(q -> q
                    .match(t -> t
                            .field("message")
                            .query(request.getMessage())));
        }
        if (request.getMinDuration() != null || request.getMaxDuration() != null) {
            queryBuilder.withFilter(q -> q
                    .range(t -> t
                            .term(v -> {
                                v.field("duration");
                                if (request.getMinDuration() == null) v.lte(request.getMaxDuration());
                                else if (request.getMaxDuration() == null) v.gte(request.getMinDuration());
                                else {
                                    v.lte(request.getMaxDuration());
                                    v.gte(request.getMinDuration());
                                }
                                return v;
                            })));
        }
        NativeQuery query = queryBuilder.build();
        SearchHits<GetTraceIdsResponseDto> searchHits = elasticsearchOperations.search(query, GetTraceIdsResponseDto.class);
        List<String> ans = searchHits.stream()
                .map(SearchHit::getContent)
                .map(GetTraceIdsResponseDto::getTraceId)
                .toList();
        System.out.println(ans);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .map(FieldValue::of)
                .distinct()
                .toList();
    }

    private SearchHits<GetTraceIdsResponseDto> getGraphs(List<FieldValue> traceIds) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        queryBuilder.withQuery(q -> q
                .bool(b -> b
                        .must(m -> m
                                .terms(ta -> ta
                                        .field("trace.id")
                                        .terms(te -> te
                                                .value(traceIds))))));

        Aggregation aggByService = Aggregation.of(a -> a
                .terms(ta -> ta.field("service"))
                .aggregations("sumDurationByService", Aggregation.of(sa -> sa.sum(s -> s.field("duration"))))
                .aggregations("countInService", Aggregation.of(cs -> cs.valueCount(s -> s.field("service")))));

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
                .aggregations("byService", aggByService));
        queryBuilder.withAggregation("byTrace", aggByTrace);

        //queryBuilder.withSort(s -> s.field(FieldSort.of(f -> f.field("byService"))));
        NativeQuery query = queryBuilder.build();
        SearchHits<GetTraceIdsResponseDto> searchHits = elasticsearchOperations.search(query, GetTraceIdsResponseDto.class);
        return searchHits;
    }

    private GetTraceResponseDto toDto(SearchHits<GetTraceIdsResponseDto> searchHits) {
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
                        serviceTermsBucket.aggregations().get("countInService").valueCount().value()
                        ))
                ;
            });
            result.add(new TraceBucket(
                    traceTermsBucket.key().stringValue(),
                    traceTermsBucket.aggregations().get("sumDurationByTrace").sum().value(),
                    traceTermsBucket.aggregations().get("startName").topHits().hits().hits().get(0).toString(),
                    traceTermsBucket.aggregations().get("minTimestamp").min().value(),
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
