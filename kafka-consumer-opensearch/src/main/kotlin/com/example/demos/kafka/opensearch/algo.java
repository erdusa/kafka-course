package com.example.demos.kafka.opensearch;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;

import java.io.IOException;

public class algo {
    public void main() throws IOException {
        // first create an OpenSearch Client
        RestHighLevelClient openSearchClient = null;
        KafkaConsumer<String, String> consumer = null;

        // we need to create the index on OpenSearch if it doesn't exist already
        try (openSearchClient; consumer){
            CreateIndexRequest createIndexRequest = null;
            openSearchClient.indices().create(createIndexRequest, org.opensearch.client.RequestOptions.DEFAULT);

        }

        // create our kafka Client

        // main code logic

        // close things
    }
}
