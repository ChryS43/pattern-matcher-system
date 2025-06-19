package com.pms.match_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatternMatchEvent {
    private PatternDTO pattern;
    private List<KeystrokeDTO> matchedKeystrokes;
    private String sessionId;
    private Instant timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternDTO {
        private String id;
        private String name;
        private String sessionId;
        private Map<String, Object> rootBlock; // Todo: Bad typing, should be a class
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeystrokeDTO {
        private String id;
        private String key;
        private Instant timestamp;
        private Long durationMs;
    }
}
