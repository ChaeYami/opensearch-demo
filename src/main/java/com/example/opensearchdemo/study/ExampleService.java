package com.example.opensearchdemo.study;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}