package com.pms.pattern_detector_sequence.service;

import org.springframework.amqp.core.Message;

public interface KeystrokeMessageListener {
    void receiveMessage(Message message);
} 