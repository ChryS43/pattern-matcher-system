package com.pms.match_event.controller;

import com.pms.match_event.entity.MatchEvent;
import com.pms.match_event.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/match-events")
@RequiredArgsConstructor
public class MatchEventController {

    private final MatchEventService matchEventService;

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<MatchEvent>> getMatchEventsBySessionId(@PathVariable String sessionId) {
        log.debug("Received request to get match events for session: {}", sessionId);
        List<MatchEvent> events = matchEventService.getMatchEventsBySessionId(sessionId);
        return ResponseEntity.ok(events);
    }
} 