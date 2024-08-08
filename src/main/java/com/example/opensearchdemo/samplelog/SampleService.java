package com.example.opensearchdemo.samplelog;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SampleService {
    private final OpenSearchClient openSearchClient;

    @Autowired
    public SampleService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public String indexDocument(String indexName, String documentId, Map<String, Object> document) throws IOException {
        IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                .index(indexName)
                .id(documentId)
                .document(document)
                .build();

        IndexResponse response = openSearchClient.index(request);
        return response.result().toString();
    }

    public SearchResponse searchAlerts(String indexName, String appliance, String name, String severity, String sortField, String sortOrder) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        if (appliance != null && !appliance.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("appliance").query(FieldValue.of(appliance)))));
        }

        if (name != null && !name.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("name").query(FieldValue.of(name)))));
        }

        if (severity != null && !severity.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("severity").query(FieldValue.of(severity)))));
        }

        List<SortOptions> sortOptions = new ArrayList<>();
        if (sortField != null && !sortField.isEmpty()) {
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field(sortField).order(SortOrder.valueOf(sortOrder.toUpperCase())))));
        }

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q.bool(b -> b.must(mustQueries)))
                .sort(sortOptions)
                .build();

        return openSearchClient.search(searchRequest, Object.class);
    }
}
