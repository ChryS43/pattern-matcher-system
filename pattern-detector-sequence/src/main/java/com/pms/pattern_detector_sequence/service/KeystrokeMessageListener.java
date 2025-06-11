package com.pms.pattern_detector_sequence.service;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;

public interface KeystrokeMessageListener {
    void receiveMessage(KeystrokeMessage message);
} 