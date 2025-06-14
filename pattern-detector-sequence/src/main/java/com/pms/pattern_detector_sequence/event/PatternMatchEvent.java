package com.pms.pattern_detector_sequence.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatternMatchEvent {
    private String patternId;
    private String patternName;
    private String sessionId;
    private Instant matchTime;
    private List<String> matchedEventIds;
} 