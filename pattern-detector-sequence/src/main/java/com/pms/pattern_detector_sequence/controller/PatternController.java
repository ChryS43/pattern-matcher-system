package com.pms.pattern_detector_sequence.controller;

import com.pms.pattern_detector_sequence.dto.PatternRequest;
import com.pms.pattern_detector_sequence.dto.PatternResponse;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import com.pms.pattern_detector_sequence.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/patterns")
@RequiredArgsConstructor
@Tag(name = "Pattern API", description = "Handling of Pattern API")
public class PatternController {

    private final PatternService patternService;

    @PostMapping
    public ResponseEntity<PatternEntity> createPattern(@RequestBody PatternRequest request) {
        return ResponseEntity.ok(patternService.createPattern(request));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<PatternEntity>> getPatternsBySession(@PathVariable String sessionId) {
        List<PatternEntity> patterns = patternService.getPatternsBySession(sessionId);
        return patterns.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(patterns);
    }
}
