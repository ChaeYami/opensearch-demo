package com.example.opensearchdemo.demoV1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.mapping.FieldMapping;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.GetFieldMappingRequest;
import org.opensearch.client.opensearch.indices.GetFieldMappingResponse;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_field_mapping.TypeFieldMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemoService {

    private final OpenSearchClient openSearchClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public DemoService(OpenSearchClient openSearchClient, ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.objectMapper = objectMapper;
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





    /**
     * 하이브리드 검색
     * @param indexName
     * @param textField
     * @param textValue
     * @param termField
     * @param termValue
     * @param excludeField
     * @param excludeValue
     * @return
     * @throws IOException
     */
    public List<ObjectNode> searchWithHybridQuery(String indexName, String textField, String textValue,
                                                  String termField, String termValue,String excludeField, String excludeValue) throws IOException {

        // BoolQuery의 조건을 담을 리스트 생성
        List<Query> mustQueries = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();

        // MatchQuery 추가 (textField와 textValue가 null이 아니거나 빈 값이 아닐 때)
        if (textField != null && !textField.isEmpty() && textValue != null && !textValue.isEmpty()) {
            mustQueries.add(Query.of(m -> m.match(MatchQuery.of(mq -> mq
                    .field(textField)
                    .query(FieldValue.of(textValue))
            ))));
        }

        // TermQuery 추가 (termField와 termValue가 null이 아니거나 빈 값이 아닐 때)
        if (termField != null && !termField.isEmpty() && termValue != null && !termValue.isEmpty()) {
            mustQueries.add(Query.of(t -> t.term(TermQuery.of(tq -> tq
                    .field(termField)
                    .value(FieldValue.of(termValue))
            ))));
        }


        // 부정형 검색 추가 (excludeField와 excludeValue가 null이 아니거나 빈 값이 아닐 때)
        if (excludeField != null && !excludeField.isEmpty() && excludeValue != null && !excludeValue.isEmpty()) {
            mustNotQueries.add(Query.of(en -> en.term(TermQuery.of(tq -> tq
                    .field(excludeField)
                    .value(FieldValue.of(excludeValue))
            ))));
        }

        // BoolQuery로 각 쿼리를 조합
        Query searchQuery = Query.of(q -> q.bool(BoolQuery.of(b -> b
                .must(mustQueries)
                .mustNot(mustNotQueries)
        )));

        // SearchRequest 생성
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(indexName)
                .query(searchQuery)
                .build();

        // 검색 요청 및 응답 처리
        SearchResponse<ObjectNode> searchResponse = openSearchClient.search(searchRequest, ObjectNode.class);

        // 검색된 결과 반환
        return searchResponse.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }


    /**
     * 필드의 타입을 가져옵니다.
     * @param index 인덱스명
     * @return 필드 타입
     * @throws IOException
     */
    public  Map<String, String> getFieldType(String index) throws IOException {


        // 결과를 저장할 Map 생성
        Map<String, String> fieldTypesMap = new HashMap<>();

        // GetMappingRequest 생성
        GetMappingRequest request = new GetMappingRequest.Builder().index(index).build();
        GetMappingResponse response = openSearchClient.indices().getMapping(request);

        // 인덱스의 매핑 정보에서 properties 가져오기
        Map<String, Property> properties = response.result().get(index).mappings().properties();

        // 모든 필드와 타입 저장 (재귀적 탐색)
        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            String field = entry.getKey();
            Property property = entry.getValue();
            storeFieldType(field, property, "", fieldTypesMap);
        }

        // 필드:타입 맵 반환
        return fieldTypesMap;
    }

    // 재귀적으로 필드와 타입을 저장하는 메서드
    private void storeFieldType(String fieldName, Property property, String parentPath, Map<String, String> fieldTypesMap) {
        // 현재 필드의 전체 경로 계산
        String fullPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;

        // 필드의 타입 저장
        String fieldType = property._kind().toString();
        fieldTypesMap.put(fullPath, fieldType);

        // ObjectProperty 또는 NestedProperty일 경우, 각각의 하위 필드 탐색
        if (property.isObject()) {
            Map<String, Property> nestedProperties = property.object().properties();
            if (nestedProperties != null) {
                for (Map.Entry<String, Property> nestedEntry : nestedProperties.entrySet()) {
                    storeFieldType(nestedEntry.getKey(), nestedEntry.getValue(), fullPath, fieldTypesMap);
                }
            }
        } else if (property.isNested()) {
            Map<String, Property> nestedProperties = property.nested().properties();
            if (nestedProperties != null) {
                for (Map.Entry<String, Property> nestedEntry : nestedProperties.entrySet()) {
                    storeFieldType(nestedEntry.getKey(), nestedEntry.getValue(), fullPath, fieldTypesMap);
                }
            }
        }
    }

}
