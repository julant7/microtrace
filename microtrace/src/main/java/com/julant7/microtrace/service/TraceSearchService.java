package com.julant7.microtrace.service;

import com.julant7.microtrace.dto.GetTraceRequest;
import com.julant7.microtrace.model.Span;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Service
public class TraceSearchService {
    /**
     * {
     *   "aggs”: {
     *     “name_of_aggregation”: {
     *       “type_of_aggregation”: {
     *         “field”: “document_field_name”
     *          }
     *         }
     *   }
     * }
     */
    private final ElasticsearchOperations elasticsearchOperations;
    public List<Span> findTracesByFilters(GetTraceRequest request, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                // .withFilter()
                .withAggregation("grades_stats")
                .build();
        List<String> traceIds = getTraceIdsByFilters(request, pageable);
        return getGraphs(traceIds, pageable);
    }

    private List<String> getTraceIdsByFilters(GetTraceRequest request, Pageable pageable) {
        Instant now = Instant.now();
        // с помощью этих критериев
        Criteria criteria = new Criteria();
        if (request.getTimeIntervals() != null) {
            Criteria criteriaToAdd  = new Criteria("timestamp");
            switch (request.getTimeIntervals()) {
                case MINUTES_5 -> criteriaToAdd.between(now.minus(5, ChronoUnit.MINUTES), now);
                case MINUTES_15 -> criteriaToAdd.between(now.minus(15, ChronoUnit.MINUTES), now);
                case HOURS_1 -> criteriaToAdd.between(now.minus(1, ChronoUnit.HOURS), now);
                case HOURS_3 -> criteriaToAdd.between(now.minus(3, ChronoUnit.HOURS), now);
                case HOURS_6 -> criteriaToAdd.between(now.minus(6, ChronoUnit.HOURS), now);
                case HOURS_12 -> criteriaToAdd.between(now.minus(12, ChronoUnit.HOURS), now);
                case DAY_1 -> criteriaToAdd.between(now.minus(1, ChronoUnit.DAYS), now);
                case DAYS_3 -> criteriaToAdd.between(now.minus(3, ChronoUnit.DAYS), now);
                case DAYS_7 -> criteriaToAdd.between(now.minus(7, ChronoUnit.DAYS), now);
            }
            criteria = criteria.and(criteriaToAdd);
        }
        if (request.getService() != null) {
            criteria = criteria.and(new Criteria("service").is(request.getService()));
        }
        if (request.getServiceClass() != null) {
            criteria = criteria.and(new Criteria("scope.name").is(request.getServiceClass()));
        }
        if (request.getMessage() != null) {
            criteria = criteria.and(new Criteria("message").contains(request.getMessage()));
        }
        if (request.getMinDuration() != null || request.getMaxDuration() != null) {
            if (request.getMinDuration() == null) {
                criteria = criteria.and(new Criteria("duration").lessThanEqual(request.getMaxDuration()));
            } else if (request.getMaxDuration() == null) {
                criteria = criteria.and(new Criteria("duration").greaterThanEqual(request.getMinDuration()));
            } else {
                criteria = criteria.and(new Criteria("duration").between(request.getMinDuration(), request.getMaxDuration()));
            }
        }
        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        SearchHits<Span> searchHits = elasticsearchOperations.search(query, Span.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .map(Span::getTraceId)
                .distinct()
                .toList();
    }

    private List<Span> getGraphs(List<String> traceIds, Pageable pageable) {
        Criteria criteria = new Criteria("trace.id").in(traceIds);
        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        SearchHits<Span> searchHits = elasticsearchOperations.search(query, Span.class);
        return searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
    }




}
