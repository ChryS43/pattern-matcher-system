package com.pms.pattern_detector_sequence.repository;

import com.pms.pattern_detector_sequence.entity.PatternEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PatternRepository extends MongoRepository<PatternEntity, String> {
    List<PatternEntity> findBySessionId(String sessionId);
}
