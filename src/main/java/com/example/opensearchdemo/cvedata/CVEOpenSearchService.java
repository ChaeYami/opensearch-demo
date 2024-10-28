package com.example.opensearchdemo.cvedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CVEOpenSearchService {

    private final OpenSearchClient openSearchClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public CVEOpenSearchService(OpenSearchClient openSearchClient, ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.objectMapper = objectMapper;
    }

    /**
     * CVE 데이터를 OpenSearch에 저장
     * @param cveData 저장할 CVE 데이터
     * @param index 인덱스 이름
     * @throws IOException
     */
    public void saveCVEData(String cveData, String index) throws IOException {
        // JSON 문자열을 JsonNode로 변환
        JsonNode jsonNode = objectMapper.readTree(cveData);

        // OpenSearch에 데이터 저장
        IndexResponse indexResponse = openSearchClient.index(i -> i
                .index(index)
                .document(jsonNode)
        );

        if (indexResponse.result().equals(Result.Created)) {
            log.info("Successfully stored CVE data in OpenSearch.");
        } else {
            log.info("Failed to store CVE data in OpenSearch.");
        }
    }
}
