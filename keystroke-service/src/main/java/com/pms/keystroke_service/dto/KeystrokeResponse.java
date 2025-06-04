package com.pms.keystroke_service.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeystrokeResponse {
    private UUID id;
    private UUID sessionId;
    private String key;
    private Instant timestamp;
    private Long durationMs;
}
