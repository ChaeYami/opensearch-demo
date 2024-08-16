package com.example.opensearchdemo.samplelog.service;

import com.example.opensearchdemo.samplelog.dto.AlertDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.ScriptSortType;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SampleService {
    private final OpenSearchClient openSearchClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SampleService(OpenSearchClient openSearchClient, ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.objectMapper = objectMapper;
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



    

    public SearchResponse<AlertDTO> searchAlerts(String indexName, String appliance, String name, String severity, String sortField, String sortOrder) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        if (appliance != null && !appliance.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("appliance").query(FieldValue.of(appliance)))));
        }

        if (name != null && !name.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("alert.explanation.malware-detected.malware.name").query(FieldValue.of(name)))));
        }

        if (severity != null && !severity.isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("alert.severity").query(FieldValue.of(severity)))));
        }

        List<SortOptions> sortOptions = new ArrayList<>();

        if (sortField != null && sortOrder != null) {
            if ("occurred".equals(sortField)) {

                // 날짜 필드 정렬
                sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("alert.occurred")
                        .order(sortOrder.equalsIgnoreCase("desc") ? SortOrder.Desc : SortOrder.Asc))));
            } else if ("appliance-id".equals(sortField)) {

                // 알파벳순 정렬 (keyword 서브필드 사용)
                sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("appliance-id.keyword")
                        .order(sortOrder.equalsIgnoreCase("desc") ? SortOrder.Desc : SortOrder.Asc))));

            } else if ("severity".equals(sortField)) {

                // severity 커스텀 정렬 (keyword 서브필드 사용)
                sortOptions.add(SortOptions.of(s -> s.script(sc -> sc.type(ScriptSortType.Number)
                        .script(script -> script.inline(i -> i.source(
                                "if (doc['alert.severity.keyword'].size() == 0) { return 3; } " + // 없으면 기본 값
                                        "if (doc['alert.severity.keyword'].value == 'crit') { return 0; } " +
                                        "else if (doc['alert.severity.keyword'].value == 'majr') { return 1; } " +
                                        "else if (doc['alert.severity.keyword'].value == 'minr') { return 2; } " +
                                        "else { return 3; }"
                        )))
                        .order(sortOrder.equalsIgnoreCase("desc") ? SortOrder.Desc : SortOrder.Asc))));
            }
        }

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q.bool(b -> b.must(mustQueries)))
                .sort(sortOptions)
                .build();

        return openSearchClient.search(searchRequest, AlertDTO.class);
    }
}
