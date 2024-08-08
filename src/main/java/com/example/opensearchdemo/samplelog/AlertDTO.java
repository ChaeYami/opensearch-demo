package com.example.opensearchdemo.samplelog;

public class AlertDTO {
    private String applianceId;   // 장비의 고유 ID
    private String appliance;     // 장비의 호스트명
    private SrcDetails src;       // 소스 IP와 호스트 정보, VLAN 정보
    private DstDetails dst;       // 목적지 IP 정보
    private String explanation;   // 경고 상세 설명
    private String occurred;      // 경보 발생 시간
    private String name;          // 경고 이름
    private String severity;      // 심각도

    // Getter와 Setter 생략
}

