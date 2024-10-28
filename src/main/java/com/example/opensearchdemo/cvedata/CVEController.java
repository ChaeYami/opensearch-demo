package com.example.opensearchdemo.cvedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cve")
public class CVEController {

    private final CVEDataService cveDataService;
    private final CVEOpenSearchService cveOpenSearchService;

    @Autowired
    public CVEController(CVEDataService cveDataService, CVEOpenSearchService cveOpenSearchService) {
        this.cveDataService = cveDataService;
        this.cveOpenSearchService = cveOpenSearchService;
    }

    /**
     * CVE 데이터 가져오기 테스트
     */
    @GetMapping("/get")
    public String getCVEData(
            @RequestParam(required = false, defaultValue = "") String cpe
    ) throws JsonProcessingException {
            // NVD API에서 CVE 데이터 가져오기
            String cveData = cveDataService.getAllCVEData(cpe);

            return cveData;
    }

    /**
     * CVE 데이터를 가져와 OpenSearch에 저장
     * @param cpe CVE ID
     * @return 저장 결과
     */
    @PostMapping("/store")
    public ResponseEntity<String> storeCVEData(
            @RequestParam(required = false, defaultValue = "") String cpe
    ) {
        try {
            // NVD API에서 모든 페이지의 CVE 데이터 가져오기
            String cveData = cveDataService.getAllCVEData(cpe);

            // OpenSearch에 데이터 저장
            cveOpenSearchService.saveCVEData(cveData, "cve-test-1024");

            return ResponseEntity.ok("CVE 데이터 저장 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("CVE 데이터 저장 실패: " + e.getMessage());
        }
    }
}
