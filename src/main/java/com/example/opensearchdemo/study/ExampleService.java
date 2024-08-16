package com.example.opensearchdemo.study;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExampleService {

    private final OpenSearchClient openSearchClient;

    @Autowired
    public ExampleService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public ExampleEntity save(ExampleEntity entity) throws Exception {
        IndexRequest<ExampleEntity> indexRequest = new IndexRequest.Builder<ExampleEntity>()
                .index("example")
                .id(entity.getId())
                .document(entity)
                .build();

        IndexResponse indexResponse = openSearchClient.index(indexRequest);
        return entity;
    }

    public ExampleEntity findById(String id) throws Exception {
        GetRequest getRequest = new GetRequest.Builder()
                .index("example")
                .id(id)
                .build();

        GetResponse<ExampleEntity> getResponse = openSearchClient.get(getRequest, ExampleEntity.class);
        return getResponse.source();  // source() 메서드 사용
    }

    public List<ExampleEntity> findByCompany(String company) throws Exception {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("example")
                .query(q -> q
                        .term(t -> t
                                .field("company.keyword")
                                .value(FieldValue.of(company))
                        )
                )
                .build();

        SearchResponse<ExampleEntity> searchResponse = openSearchClient.search(searchRequest, ExampleEntity.class);
        return searchResponse.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    public List<ObjectNode> searchWithHybridQueryExample(String indexName, String textField, String textValue,
                                                         String termField, String termValue, String embeddingField,
                                                         String queryText, String modelId, int k) throws IOException {

        // 각 쿼리를 조합하여 BoolQuery로 변경
        Query searchQuery = Query.of(q -> q.bool(BoolQuery.of(b -> b
                .must(Query.of(m -> m.match(MatchQuery.of(mq -> mq
                        .field(textField)
                        .query(FieldValue.of(textValue))
                ))))
                .must(Query.of(t -> t.term(TermQuery.of(tq -> tq
                        .field(termField)
                        .value(FieldValue.of(termValue))
                ))))
                .should(Query.of(n -> n.neural(NeuralQuery.of(nq -> nq
                        .field(embeddingField)
                        .queryText(queryText)
                        .modelId(modelId)
                        .k(k)
                ))))
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


}