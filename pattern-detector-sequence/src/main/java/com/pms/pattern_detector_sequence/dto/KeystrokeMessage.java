package com.pms.pattern_detector_sequence.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class KeystrokeMessage {
    private UUID id;
    private UUID sessionId;
    private String key;
    private Instant timestamp;
    private Long durationMs;
} 