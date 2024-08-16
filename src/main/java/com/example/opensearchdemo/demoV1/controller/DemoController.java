package com.example.opensearchdemo.demoV1.controller;

import com.example.opensearchdemo.demoV1.service.DemoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/opensearch")
public class DemoController {

    private final DemoService demoService;

    @Autowired
    public DemoController(DemoService demoService) {
        this.demoService = demoService;
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
        List<JsonNode> result = demoService.searchIndex(indexName);

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
        List result = demoService.getFields(indexName);

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

        List<ObjectNode> searchResult = demoService.searchWithFilters(indexName, fieldName, value);
        return ResponseEntity.ok(searchResult);

    }

    @GetMapping("/search")
    public List<ObjectNode> search(
            @RequestParam String indexName,
            @RequestParam(required = false) String textField,
            @RequestParam(required = false) String textValue,
            @RequestParam(required = false) String termField,
            @RequestParam(required = false) String termValue,
            @RequestParam(required = false) String excludeField,
            @RequestParam(required = false) String excludeValue
    ) throws IOException {
        return demoService.searchWithHybridQuery(
                indexName,
                textField,
                textValue,
                termField,
                termValue,
                excludeField,
                excludeValue
        );
    }
}
