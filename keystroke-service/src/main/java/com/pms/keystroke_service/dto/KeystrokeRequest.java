package com.pms.keystroke_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeystrokeRequest {
    private UUID sessionId;
    private String key;
    private Long durationMs;
}
