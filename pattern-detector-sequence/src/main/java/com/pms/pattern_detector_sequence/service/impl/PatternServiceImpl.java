package com.pms.pattern_detector_sequence.service.impl;

import com.pms.pattern_detector_sequence.dto.PatternRequest;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import com.pms.pattern_detector_sequence.repository.PatternRepository;
import com.pms.pattern_detector_sequence.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {

    private final PatternRepository repository;

    @Override
    public PatternEntity createPattern(PatternRequest request) {
        PatternEntity entity = new PatternEntity();
        entity.setName(request.getName());
        entity.setSessionId(request.getSessionId());
        entity.setRootBlock(request.getRootBlock());
        return repository.save(entity);
    }

    @Override
    public List<PatternEntity> getPatternsBySession(String sessionId) {
        return repository.findBySessionId(sessionId);
    }
}