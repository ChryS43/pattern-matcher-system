package com.pms.pattern_detector_sequence.service;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import java.util.List;

public interface KeystrokeBufferService {
    void addEvent(String sessionId, KeystrokeMessage event);
    List<KeystrokeMessage> getEvents(String sessionId);
    void clearEvents(String sessionId);
} 