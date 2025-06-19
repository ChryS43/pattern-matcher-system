package com.pms.pattern_detector_sequence.event;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatternMatchEvent {
    private PatternEntity pattern;
    private List<KeystrokeMessage> matchedKeystrokes;
    private String sessionId;
    private Instant timestamp;
} 