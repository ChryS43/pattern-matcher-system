package com.pms.match_event.service.impl;

import com.pms.match_event.entity.MatchEvent;
import com.pms.match_event.repository.MatchEventRepository;
import com.pms.match_event.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchEventServiceImpl implements MatchEventService {

    private final MatchEventRepository matchEventRepository;

    @Override
    public MatchEvent saveMatchEvent(MatchEvent matchEvent) {
        log.debug("Saving match event: patternId={}, sessionId={}", 
                matchEvent.getPatternId(), matchEvent.getSessionId());
        return matchEventRepository.save(matchEvent);
    }

    @Override
    public List<MatchEvent> getMatchEventsBySessionId(String sessionId) {
        log.debug("Getting match events for session: {}", sessionId);
        return matchEventRepository.findBySessionId(sessionId);
    }
} 