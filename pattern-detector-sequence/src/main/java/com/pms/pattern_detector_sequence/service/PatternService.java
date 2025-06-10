package com.pms.pattern_detector_sequence.service;

import com.pms.pattern_detector_sequence.dto.PatternRequest;
import com.pms.pattern_detector_sequence.entity.PatternEntity;

import java.util.List;

public interface PatternService {
    PatternEntity createPattern(PatternRequest request);
    List<PatternEntity> getPatternsBySession(String sessionId);
}