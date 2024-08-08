package com.example.opensearchdemo.samplelog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    @JsonProperty("src")
    private SrcDetails src;       // 소스 IP와 호스트 정보, VLAN 정보
    private DstDetails dst;       // 목적지 IP 정보

    private MalwareExplanation explanation;   // 경고 상세 설명

//    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String occurred;      // 경보 발생 시간
    private String name;          // 경고 이름
    private String severity;      // 심각도
}
