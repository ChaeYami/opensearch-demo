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

    /**
     * 인덱스 검색 (전체 문서 가져오기)
     * @param index
     * @return
     * @throws IOException
     */
    public List<JsonNode> searchIndex(String index) throws IOException {
        // OpenSearch에서 검색 요청
        SearchResponse<JsonNode> searchResponse = openSearchClient.search(
                b -> b.index(index), JsonNode.class
        );

        // 검색 결과를 JSON 형태로 변환
        List<Hit<JsonNode>> hits = searchResponse.hits().hits();
        List<JsonNode> resultList = new ArrayList<>();

        for (Hit<JsonNode> hit : hits) {
            resultList.add(hit.source());
        }

        return resultList;
    }

    /**
     * 필드값 가져오기
     * @param index 인덱스명
     * @return
     * @throws IOException
     */
    public List<String> getFields(String index) throws IOException {
        // OpenSearch에서 첫 번째 문서 가져오기
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(index)
                .sort(s -> s.field(f -> f.field("_id").order(SortOrder.Asc)))  // 정렬 조건: _id 기준으로 오름차순
                .size(1)  // 결과 개수 제한: 1
                .build();

        // 검색 요청
        SearchResponse<ObjectNode> searchResponse = openSearchClient.search(
                searchRequest, ObjectNode.class
        );

        // 검색 결과에서 첫 번째 문서만
        List<Hit<ObjectNode>> hits = searchResponse.hits().hits();
        ObjectNode resultNode = objectMapper.createObjectNode();
        ArrayNode hitsArray = resultNode.putArray("hits");

        if (!hits.isEmpty()) {
            hitsArray.add(hits.get(0).source());
        }

        // JSON 변환
        return extractFieldNames(objectMapper.writeValueAsString(resultNode));
    }

    public List<String> extractFieldNames(String json) throws IOException {
        // JSON 문자열을 JsonNode로 변환
        JsonNode rootNode = objectMapper.readTree(json);

        // "hits" 배열을 가져와 필드 이름 추출
        JsonNode hitsNode = rootNode.path("hits");
        List<String> fieldNames = new ArrayList<>();

        if (hitsNode.isArray()) {
            for (JsonNode hitNode : hitsNode) {
                extractFieldNames(hitNode, "", fieldNames);
            }
        }

        return fieldNames;
    }

    private void extractFieldNames(JsonNode node, String parentPath, List<String> fieldNames) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                String newPath = parentPath.isEmpty() ? key : parentPath + "." + key;

                if (value.isObject() || value.isArray()) {
                    // 객체 또는 배열일 경우 재귀적으로 탐색
                    extractFieldNames(value, newPath, fieldNames);
                } else {
                    // 기본 필드일 경우 리스트에 추가
                    fieldNames.add(newPath);
                }
            }
        } else if (node.isArray()) {
            // 배열일 경우 각 요소를 재귀적으로 탐색
            for (JsonNode arrayElement : node) {
                extractFieldNames(arrayElement, parentPath, fieldNames);
            }
        }
    }


    /**
     * 검색
     * @param indexName 인덱스명
     * @param fieldName 필드명
     * @param value 검색할 필드값
     * @return
     * @throws IOException
     */
    public List<ObjectNode> searchWithFilters(String indexName, String fieldName, String value) throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(q -> q.match(m -> m.field(fieldName).query(FieldValue.of(value))))
                .build();

        SearchResponse<ObjectNode> searchResponse = openSearchClient.search(searchRequest, ObjectNode.class);

        return searchResponse.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
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
