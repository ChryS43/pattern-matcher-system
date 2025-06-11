package com.pms.keystroke_service.service.impl;

import com.pms.keystroke_service.config.RabbitMQConfig;
import com.pms.keystroke_service.dto.KeystrokeResponse;
import com.pms.keystroke_service.service.KeystrokeService;
import com.pms.keystroke_service.service.RabbitMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import java.util.UUID;

@Slf4j
@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final KeystrokeService keystrokeService;

    @Value("${keystroke.queue.name}")
    private String queueName;

    public RabbitMQServiceImpl(RabbitTemplate rabbitTemplate, KeystrokeService keystrokeService) {
        this.rabbitTemplate = rabbitTemplate;
        this.keystrokeService = keystrokeService;

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message successfully delivered to queue {}", queueName);
            } else {
                log.error("Failed to deliver message to queue {}. Cause: {}", queueName, cause);
            }
        });
    }

    @Override
    public void emitKeystroke(UUID keystrokeId) {
        try {
            KeystrokeResponse keystroke = keystrokeService.getById(keystrokeId);

            log.info("Attempting to send keystroke {} to queue {}", keystrokeId, queueName);

            rabbitTemplate.convertAndSend(queueName, keystroke);
            log.info("Keystroke {} sent to queue {}", keystrokeId, queueName);
        } catch (Exception e) {
            log.error("Error while emitting keystroke {}: {}", keystrokeId, e.getMessage(), e);
            throw e;
        }
    }
}
