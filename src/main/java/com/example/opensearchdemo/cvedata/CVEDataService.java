package com.example.opensearchdemo.cvedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CVEDataService {

    private final RestTemplate restTemplate;

    private final String apiKey = "da3606e8-7cd0-4acd-8a52-3b53baafc497"; //  API Key
    private final String baseUrl = "https://services.nvd.nist.gov/rest/json/cves/2.0";

    public CVEDataService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * NVD API로 CVE 데이터 받아오기 (한 페이지)
     * @param cpe CPE 이름
     * @param startIndex 시작 인덱스 (페이징 처리)
     * @return CVE 데이터 (한 페이지)
     */
    public String getCVEData(String cpe, Long startIndex) {
//        String url = baseUrl + "?cpeName=" + cpe + "&startIndex=" + startIndex + "&resultsPerPage=2000";

        Long resultsPerPage = 10L;
        // 기본 URL 시작
        StringBuilder urlBuilder = new StringBuilder(baseUrl + "?resultsPerPage=" + resultsPerPage);

        // cpe 파라미터가 존재하면 URL에 추가
        if (cpe != null && !cpe.isEmpty()) {
            urlBuilder.append("&cpeName=").append(cpe);
        }

        // 최종 URL 생성
        String url = urlBuilder.toString();

        log.info("URL : {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("apiKey", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API에서 데이터 가져오기
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        log.info("success get CVE Data");
        // API 응답 반환
        return response.getBody();
    }

    // startIndex만 받을 때
    public String getCVEData(Long startIndex) {
        return getCVEData(null, startIndex);
    }

    /**
     * NVD API로 전체 CVE 데이터 가져오기
     * @param cpe CPE 이름
     * @return 모든 페이지의 CVE 데이터 합친 것
     */
    public String getAllCVEData(String cpe) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> vulnerabilities = new ArrayList<>(); // 전체 vulnerabilities 저장할 리스트

        StringBuilder firstPageData = new StringBuilder();
        // 첫 페이지 데이터 가져오기
        if (cpe != null && !cpe.isEmpty()) {
            firstPageData.append(getCVEData(cpe, 0L));

        } else {
            firstPageData.append(getCVEData(0L));
        }
        JsonNode firstPageJson = objectMapper.readTree(String.valueOf(firstPageData));

        // 전체 결과 개수 가져오기
        Long resultsPerPage = firstPageJson.get("resultsPerPage").asLong();
        Long totalResults = firstPageJson.get("totalResults").asLong();

        // 첫 페이지의 vulnerabilities 추가 (ArrayNode 순회)
        ArrayNode firstPageVulnerabilities = (ArrayNode) firstPageJson.get("vulnerabilities");
        firstPageVulnerabilities.forEach(vulnerabilities::add);

        log.info("Success get first Page Data");

        StringBuilder totalPageNum = new StringBuilder();
        // 여러 페이지인 경우 처리 (totalResults > resultsPerPage일 때)
        if (totalResults > resultsPerPage) {
            // 총 페이지 수 계산 (totalResults / resultsPerPage)
            Long totalPage = totalResults / resultsPerPage;

            totalPageNum.append(totalPage+1);

            log.info("Total Page : {}", totalPage);

            // 나머지 페이지 데이터 가져오기
            for (Long startIndex = 1L; startIndex <= totalPage; startIndex++) {
                log.info("---------- Current start Index : {}", startIndex);

                StringBuilder pageData = new StringBuilder();
                if (cpe != null && !cpe.isEmpty()) {
                    pageData.append(getCVEData(cpe, startIndex * resultsPerPage));
                } else {
                    pageData.append(getCVEData(startIndex * resultsPerPage));
                }
                JsonNode pageJson = objectMapper.readTree(String.valueOf(pageData));

                // 각 페이지의 vulnerabilities 추가 (ArrayNode 순회)
                ArrayNode pageVulnerabilities = (ArrayNode) pageJson.get("vulnerabilities");
                pageVulnerabilities.forEach(vulnerabilities::add);

                log.info("Success get new page : {} ----------", startIndex);
            }
        }


        // 모든 vulnerabilities를 하나의 JsonNode로 합치기
        ObjectNode result = objectMapper.createObjectNode();
        result.put("totalResults", totalResults);
        result.put("resultsPerPage", resultsPerPage);
        try {
            result.put("totalPage", totalPageNum == null || totalPageNum.isEmpty() ? 1 : Integer.parseInt(String.valueOf(totalPageNum)));
        } catch (NumberFormatException e) {
            // 로그를 남기거나 기본값을 설정
            result.put("totalPage", 1);  // 기본값 설정
        }

        result.set("vulnerabilities", objectMapper.valueToTree(vulnerabilities));

        // JSON 문자열로 변환하여 반환
        return objectMapper.writeValueAsString(result);
    }

    public String getAllCVEData() throws JsonProcessingException {
        return getAllCVEData(null);
    }
}
