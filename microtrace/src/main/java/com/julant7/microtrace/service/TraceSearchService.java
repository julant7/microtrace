package com.julant7.microtrace.service;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.julant7.microtrace.dto.FieldCondition;
import com.julant7.microtrace.dto.Field;
import com.julant7.microtrace.dto.GetSpansInTraceRequestDto;
import com.julant7.microtrace.dto.GetTraceByFilterRequest;
import com.julant7.microtrace.dto.LogicalCondition;
import com.julant7.microtrace.dto.LogicalOperator;
import com.julant7.microtrace.dto.SearchCondition;
import com.julant7.microtrace.dto.TraceSearchRequestDto;
import com.julant7.microtrace.model.Span;
import com.julant7.microtrace.dto.GetTraceResponseDto;
import com.julant7.microtrace.dto.ServiceBucket;
import com.julant7.microtrace.dto.TraceBucket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class TraceSearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public GetTraceResponseDto findTracesByFilters(TraceSearchRequestDto request) {
        List<co.elastic.clients.elasticsearch._types.FieldValue> traceIds = getTraceIdsByFilters(request);
//        SearchHits<Span> searchHits = getGraphs(traceIds, request);
//        return toDto(searchHits);
        return null;
    }

    private List<Query> extractFieldConditionsFromLogicalCondition(SearchCondition condition) {
        if (condition instanceof FieldCondition) {
            return List.of(toQuery((FieldCondition) condition));
        }
        List<Query> queries = new ArrayList<>();
        LogicalCondition logicalCondition = (LogicalCondition) condition;
        logicalCondition.conditions().forEach(innerCondition ->
                queries.addAll(extractFieldConditionsFromLogicalCondition(innerCondition)));
        return queries;
    }

    private Query insertLogicalCondition(SearchCondition condition) {
        if (condition instanceof FieldCondition) {
            return toQuery((FieldCondition) condition);
        }
        List<Query> queries = new ArrayList<>();
        LogicalCondition logicalCondition = (LogicalCondition) condition;
        logicalCondition.conditions().forEach(innerCondition -> queries.add(insertLogicalCondition(innerCondition)));
        BoolQuery.Builder finalQuery = new BoolQuery.Builder();
        if (logicalCondition.operator() == LogicalOperator.AND) {
            finalQuery.must(queries);
        } else {
            finalQuery.should(queries);
        }
        return finalQuery.build()._toQuery();
    }

    private Query toQuery(FieldCondition fieldCondition) {
        Query.Builder queryBuilder = new Query.Builder();
        Field fieldValue = fieldCondition.field();
        if (!fieldCondition.operator().supports(fieldValue.getType())) {
            throw new IllegalStateException("");
        }
        String field = fieldValue.getElasticsearchFieldName();
        Object value = fieldCondition.value();
        switch (fieldCondition.operator()) {
            case EQ -> {
                queryBuilder.match(m -> m
                        .field(field)
                        .query(value.toString()));
            }
            case NE -> queryBuilder
                    .bool(b -> b
                            .mustNot(m -> m
                                    .term(t -> t
                                            .field(field)
                                            .value(value.toString())
                                    )));
            case GT -> queryBuilder
                    .range(v -> v
                            .term(t -> t
                                    .field(field)
                                    .gt(value.toString())
                            ));
            case LT -> queryBuilder
                    .range(v -> v
                            .term(t -> t
                                    .field(field)
                                    .lt(value.toString())
                            ));
            case GTE -> queryBuilder.range(v -> v
                    .term(t -> t
                            .field(field)
                            .gte(value.toString())
                    ));
            case LTE -> queryBuilder
                    .range(v -> v
                            .term(t -> t
                                    .field(field)
                                    .lte(value.toString())
                            ));
            case REGEXP -> queryBuilder
                    .regexp(q -> q
                            .field(field)
                            .queryName(value.toString())
                    );
            case IN -> {
                List<String> stringValues = (List<String>) value;
                List<co.elastic.clients.elasticsearch._types.FieldValue> values = new ArrayList<>();
                stringValues.forEach(v -> values.add(co.elastic.clients.elasticsearch._types.FieldValue.of(v)));
                queryBuilder
                        .bool(b -> b
                                .must(m -> m
                                        .terms(ta -> ta
                                                .field(field)
                                                .terms(te -> te
                                                        .value(values)
                                                ))));
            }
            case NOT_IN -> {
                List<String> stringValues = (List<String>) value;
                List<co.elastic.clients.elasticsearch._types.FieldValue> values = new ArrayList<>();
                stringValues.forEach(v -> values.add(co.elastic.clients.elasticsearch._types.FieldValue.of(v)));
                queryBuilder
                        .bool(b -> b
                                .mustNot(m -> m
                                        .terms(ta -> ta
                                                .field(field)
                                                .terms(te -> te
                                                        .value(values)
                                                ))));
            }
            case EXISTS -> {
                queryBuilder
                        .bool(b -> b
                                .filter(f -> f
                                        .exists(e -> e
                                                .field(field)
                                        )));
            }
            case NOT_EXISTS -> {
                queryBuilder
                        .bool(b -> b
                                .mustNot(m -> m
                                        .exists(e -> e
                                                .field(field)
                                        )));
            }
        }
        return queryBuilder.build();
    }
    private List<FieldValue> getTraceIdsByFilters(TraceSearchRequestDto request) {
        var startQuery = request.query();
        // извлекаем все FieldCondition для общего примитивного should-фильтра
        List<Query> preFilterQueriesForShould = extractFieldConditionsFromLogicalCondition(startQuery);
        NativeQueryBuilder preFilterQueryBuilder = NativeQuery.builder();
        preFilterQueryBuilder.withFields("trace.id");
        preFilterQueryBuilder.withQuery(q -> q
                .bool(v -> v
                .should(preFilterQueriesForShould)
                ));

        SearchHits<Span> searchHits = elasticsearchOperations.search(preFilterQueryBuilder.build(), Span.class);

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
//
//        NativeQueryBuilder queryBuilder = NativeQuery.builder();
//        queryBuilder.withFields("trace.id");
//        System.out.println(request.getTimeInterval() != null);
//
//        if (request.getTimeInterval() != null) {
//            System.out.println(request.getTimeInterval() == MINUTES_5);
//            Instant now = Instant.now();
//            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
//            queryBuilder.withFilter(q -> q
//                    .range(r -> r
//                            .date(v -> {
//                                v.field("@timestamp")
//                                        .lte(formatter.format(now));
//                                switch (request.getTimeInterval()) {
//                                    case MINUTES_5  -> v.gte(formatter.format(now.minus(5, ChronoUnit.MINUTES)));
//                                    case MINUTES_15 -> v.gte(formatter.format(now.minus(15, ChronoUnit.MINUTES)));
//                                    case HOURS_1 -> v.gte(formatter.format(now.minus(1, ChronoUnit.HOURS)));
//                                    case HOURS_3 -> v.gte(formatter.format(now.minus(3, ChronoUnit.HOURS)));
//                                    case HOURS_6 -> v.gte(formatter.format(now.minus(6, ChronoUnit.HOURS)));
//                                    case HOURS_12 -> v.gte(formatter.format(now.minus(12, ChronoUnit.HOURS)));
//                                    case DAY_1 -> v.gte(formatter.format(now.minus(1, ChronoUnit.DAYS)));
//                                    case DAYS_3 -> v.gte(formatter.format(now.minus(3, ChronoUnit.DAYS)));
//                                }
//                                return v;
//                            })));
//        }
//        if (request.getServices() != null) {
//            List<FieldValue> fieldValues = new ArrayList<>();
//            request.getServices().forEach(f -> fieldValues.add(FieldValue.of(f)));
//            if (request.getServiceOperator() == ServiceOperator.OR) {
//                queryBuilder.withQuery(q -> q
//                        .bool(b -> b
//                                .must(m -> m
//                                        .terms(t -> t
//                                                .field("service")
//                                                .terms(te -> te
//                                                        .value(fieldValues))))));
//            }
//        }
//        if (request.getOperation() != null) {
//            List<FieldValue> fieldValues = new ArrayList<>();
//            request.getOperation().forEach(f -> fieldValues.add(FieldValue.of(f)));
//            if (request.getLogicalOperator() == LogicalOperator.OR) {
//                queryBuilder.withQuery(q -> q
//                        .bool(b -> b
//                                .must(m -> m
//                                        .terms(t -> t
//                                                .field("service")
//                                                .terms(te -> te
//                                                        .value(fieldValues))))));
//            }
//        }
//        NativeQuery query1 = queryBuilder.build();
//        SearchHits<Span> searchHits = elasticsearchOperations.search(query1, Span.class);
//
//        System.out.println(searchHits.stream()
//                .map(SearchHit::getContent)
//                .map(Span::getTraceId)
//                .distinct()
//                .toList());
//        return searchHits.stream()
//                .map(SearchHit::getContent)
//                .map(Span::getTraceId)
//                .map(FieldValue::of)
//                .distinct()
//                .toList();
    }

    private SearchHits<Span> getGraphs(List<co.elastic.clients.elasticsearch._types.FieldValue> traceIds, GetTraceByFilterRequest request) {
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
