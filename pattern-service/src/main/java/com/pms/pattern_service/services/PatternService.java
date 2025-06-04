package com.pms.pattern_service.services;

import com.pms.pattern_service.dto.PatternRequest;
import com.pms.pattern_service.entities.Pattern;
import com.pms.pattern_service.repositories.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatternService {

    private final PatternRepository patternRepository;

    public Pattern createPattern(PatternRequest request) {
        Pattern pattern = Pattern.builder()
                .name(request.getName())
                .type(request.getType())
                .config(request.getConfig())
                .build();
        return patternRepository.save(pattern);
    }

    public List<Pattern> getAllPatterns() {
        return patternRepository.findAll();
    }
}
