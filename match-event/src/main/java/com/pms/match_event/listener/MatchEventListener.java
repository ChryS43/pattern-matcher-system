package com.pms.match_event.listener;

import com.pms.match_event.dto.PatternMatchEvent;
import com.pms.match_event.entity.MatchEvent;
import com.pms.match_event.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEventListener {

    private final MatchEventService matchEventService;

    @RabbitListener(queues = "${match-event.queue.name}")
    public void handlePatternMatchEvent(PatternMatchEvent event) {
        log.info("Received pattern match event: patternId={}, sessionId={}", 
                event.getPattern().getId(), event.getSessionId());

        MatchEvent matchEvent = MatchEvent.builder()
                .patternId(event.getPattern().getId())
                .patternName(event.getPattern().getName())
                .sessionId(event.getSessionId())
                .patternRootBlock(event.getPattern().getRootBlock())
                .matchedKeystrokes(event.getMatchedKeystrokes().stream()
                        .map(ks -> new MatchEvent.KeystrokeData(
                                ks.getId(),
                                ks.getKey(),
                                ks.getTimestamp(),
                                ks.getDurationMs()))
                        .collect(Collectors.toList()))
                .timestamp(event.getTimestamp())
                .build();

        try {
            matchEventService.saveMatchEvent(matchEvent);
            log.info("Successfully saved match event: id={}", matchEvent.getId());
        } catch (Exception e) {
            log.error("Failed to save match event: {}", e.getMessage(), e);
        }
    }
} 