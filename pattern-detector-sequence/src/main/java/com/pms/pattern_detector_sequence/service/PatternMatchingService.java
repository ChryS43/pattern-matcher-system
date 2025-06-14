package com.pms.pattern_detector_sequence.service;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import com.pms.pattern_detector_sequence.evaluator.PatternEvaluator.EvaluationResult;

import java.util.List;

public interface PatternMatchingService {
    void processKeystroke(String sessionId, KeystrokeMessage event);
    List<PatternEntity> getMatchingPatterns(String sessionId);
    void registerPattern(PatternEntity pattern);
    void unregisterPattern(String patternId);
} 