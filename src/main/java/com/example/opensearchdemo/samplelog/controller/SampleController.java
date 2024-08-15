package com.example.opensearchdemo.samplelog.controller;

import com.example.opensearchdemo.samplelog.dto.AlertDTO;
import com.example.opensearchdemo.samplelog.service.SampleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
public class SampleController {

    private final SampleService sampleService;

    @Autowired
    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/search")
    public List<AlertDTO> searchAlerts(
            @RequestParam(required = false) String appliance,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder
    ) throws IOException {
        SearchResponse<AlertDTO> response = sampleService.searchAlerts("alerts", appliance, name, severity, sortField, sortOrder);
        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * 인덱스 검색 (전체 문서 가져오기)
     * @param indexName 인덱스명
     * @return
     * @throws IOException
     */
    @GetMapping("/index")
    public List<JsonNode> searchFields(@RequestParam String indexName)
        throws IOException{
        List<JsonNode> result = sampleService.searchIndex(indexName);

        return result;

    }

    /**
     * 필드값 가져오기
     * @param indexName 인덱스명
     * @return
     * @throws IOException
     */
    @GetMapping("/index-mapping")
    public List indexMapping(@RequestParam String indexName) throws IOException {
        List result = sampleService.getFields(indexName);

        return result;
    }

    /**
     * 검색
     * @param indexName 인덱스명
     * @param fieldName 필드명
     * @param value 필드값
     * @return
     * @throws IOException
     */
    @GetMapping("/search-by")
    public ResponseEntity<List<ObjectNode>> searchWithFilters(
            @RequestParam String indexName,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String value
    ) throws IOException {

        List<ObjectNode> searchResult = sampleService.searchWithFilters(indexName, fieldName, value);
        return ResponseEntity.ok(searchResult);

    }

}