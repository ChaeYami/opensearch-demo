package com.example.opensearchdemo.samplelog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertDTO {
    @JsonProperty("appliance-id")
    private String applianceId;   // 장비의 고유 ID
    private String appliance;     // 장비의 호스트명
    private Alert alert;



}

