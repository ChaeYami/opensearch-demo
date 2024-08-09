package com.example.opensearchdemo.samplelog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertDTO {

    @JsonProperty("appliance-id")
    private String applianceId;
    private String appliance;
    private Alert alert;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alert {
        private SrcDetails src;
        private DstDetails dst;
        private MalwareExplanation explanation;
        private String occurred;
        private String name;
        private String severity;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SrcDetails {
            private String ip;
            private String host;
            private String vlan;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DstDetails {
            private String ip;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MalwareExplanation {
            @JsonProperty("malware-detected")
            private MalwareDetected malwareDetected;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MalwareDetected {
                private Malware malware;

                @Data
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Malware {
                    private String name;
                }
            }
        }
    }
}


