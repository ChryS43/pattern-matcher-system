package com.pms.pattern_detector_sequence.dto;

import com.pms.pattern_detector_sequence.entity.PatternEntity.PatternBlock;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PatternRequest {
    private String name;
    private String sessionId;
    private PatternBlock rootBlock;
}
