package com.pms.keystroke_service.service;

import java.util.UUID;

public interface RabbitMQService {
    void emitKeystroke(UUID keystrokeId);
} 