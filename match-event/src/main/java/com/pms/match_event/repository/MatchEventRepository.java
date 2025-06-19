package com.pms.match_event.repository;

import com.pms.match_event.entity.MatchEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchEventRepository extends MongoRepository<MatchEvent, String> {
    List<MatchEvent> findBySessionId(String sessionId);
} 