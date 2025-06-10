package com.pms.pattern_detector_sequence.dto;

import com.pms.pattern_detector_sequence.entity.PatternEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatternResponse {
    private PatternEntity pattern;
}