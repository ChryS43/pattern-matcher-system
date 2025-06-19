package com.pms.match_event.service;

import com.pms.match_event.entity.MatchEvent;
import java.util.List;

public interface MatchEventService {
    MatchEvent saveMatchEvent(MatchEvent matchEvent);
    List<MatchEvent> getMatchEventsBySessionId(String sessionId);
} 