package com.pms.match_event.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "match_events")
public class MatchEvent {
    @Id
    private String id;
    private String patternId;
    private String patternName;
    private String sessionId;
    private Map<String, Object> patternRootBlock;
    private List<KeystrokeData> matchedKeystrokes;
    private Instant timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeystrokeData {
        private String id;
        private String key;
        private Instant timestamp;
        private Long durationMs;
    }
} 