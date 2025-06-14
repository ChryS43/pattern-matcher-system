package com.pms.pattern_detector_sequence.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.service.KeystrokeMessageListener;
import com.pms.pattern_detector_sequence.service.PatternMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeystrokeMessageListenerImpl implements KeystrokeMessageListener {

    private final PatternMatchingService patternMatchingService;
    private final ObjectMapper objectMapper;

    @Override
    @RabbitListener(queues = "#{'${keystroke.queue.name}'}", containerFactory = "rawRabbitListenerContainerFactory")
    public void receiveMessage(Message message) {
        try {
            log.info("Received RabbitMQ message: {}", message);
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            
            byte[] payload = message.getBody();
            String jsonString = new String(payload);
            log.info("Message payload as string: {}", jsonString);
            
            KeystrokeMessage keystrokeMessage = KeystrokeMessage.fromResponse(jsonString, objectMapper);
            log.info("Converted to KeystrokeMessage: {}", keystrokeMessage);
            
            patternMatchingService.processKeystroke(keystrokeMessage.getSessionId().toString(), keystrokeMessage);
        } catch (Exception e) {
            log.error("Error processing keystroke message: {}", message, e);
        }
    }
} 