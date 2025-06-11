package com.pms.pattern_detector_sequence.service.impl;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.service.KeystrokeMessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class KeystrokeMessageListenerImpl implements KeystrokeMessageListener {

    @Override
    @RabbitListener(queues = "#{'${keystroke.queue.name}'}")
    public void receiveMessage(KeystrokeMessage message) {
        // Log all fields of the received message
        System.out.println("Received keystroke message: id=" + message.getId()
                + ", sessionId=" + message.getSessionId()
                + ", key=" + message.getKey()
                + ", timestamp=" + message.getTimestamp()
                + ", durationMs=" + message.getDurationMs());
        // TODO: Add your processing logic here
    }
} 