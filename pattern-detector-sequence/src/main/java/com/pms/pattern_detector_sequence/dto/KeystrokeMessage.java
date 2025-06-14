package com.pms.pattern_detector_sequence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeystrokeMessage {
    private UUID id;
    private UUID sessionId;
    private String key;
    private Instant timestamp;
    private Long durationMs;

    public static KeystrokeMessage fromResponse(String jsonString, ObjectMapper mapper) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, KeystrokeMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert response to KeystrokeMessage", e);
        }
    }
} 