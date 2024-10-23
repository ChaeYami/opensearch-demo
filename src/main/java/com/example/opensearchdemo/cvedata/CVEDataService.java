package com.example.opensearchdemo.cvedata;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CVEDataService {

    private final RestTemplate restTemplate;

    private final String apiKey = "da3606e8-7cd0-4acd-8a52-3b53baafc497"; //  API Key
    private final String baseUrl = "https://services.nvd.nist.gov/rest/json/cves/2.0";

    public CVEDataService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * NVD API로 CVE 데이터 받아오기
     * @param cveId CVE ID
     * @return CVE 데이터
     */
    public String getCVEData(String cpe) {
        String url = baseUrl + "?cpeName=" + cpe;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apiKey", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API에서 데이터 가져오기
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // API 응답 반환
        return response.getBody();
    }
}
