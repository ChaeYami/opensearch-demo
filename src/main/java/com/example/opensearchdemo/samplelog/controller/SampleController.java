package com.example.opensearchdemo.samplelog.controller;

import com.example.opensearchdemo.samplelog.dto.AlertDTO;
import com.example.opensearchdemo.samplelog.service.SampleService;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
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

}