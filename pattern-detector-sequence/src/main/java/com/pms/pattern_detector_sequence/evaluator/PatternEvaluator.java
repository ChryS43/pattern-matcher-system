package com.pms.pattern_detector_sequence.evaluator;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import java.util.List;

public interface PatternEvaluator {
    enum EvaluationResult {
        MATCHED,
        FAILED,
        WAITING
    }

    EvaluationResult evaluate(List<KeystrokeMessage> events, long currentTime);
} 