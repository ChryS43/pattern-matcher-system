package com.pms.match_event.service;

import com.pms.match_event.dto.PatternMatchEvent;
import com.pms.match_event.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
@Service
public class PatternMatchEventListener {

    @RabbitListener(queues = "${match-event.queue.name}")
    public void handlePatternMatchEvent(PatternMatchEvent event) {
        log.info("✅ [MatchEventService] Received PatternMatchEvent: {}", event);

        // 👉 Qui puoi aggiungere la logica reale: DB, notifica, logica di business
    }
}
