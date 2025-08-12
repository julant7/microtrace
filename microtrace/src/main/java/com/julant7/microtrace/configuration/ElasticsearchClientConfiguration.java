package com.julant7.microtrace.configuration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

//@Configuration
public class ElasticsearchClientConfiguration {
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        return ElasticsearchClients.create(
//                RestClient.builder(new HttpHost("localhost", 9200)).build()
//        );
//    }
}
