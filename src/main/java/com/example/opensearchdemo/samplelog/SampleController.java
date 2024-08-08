package com.example.opensearchdemo.samplelog;

import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/alerts")
public class SampleController {

    private final SampleService sampleService;

    @Autowired
    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/search")
    public SearchResponse searchAlerts(
            @RequestParam(required = false) String appliance,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false, defaultValue = "occurred") String sortField,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder
    ) throws IOException {
        return sampleService.searchAlerts("alerts", appliance, name, severity, sortField, sortOrder);
    }
}