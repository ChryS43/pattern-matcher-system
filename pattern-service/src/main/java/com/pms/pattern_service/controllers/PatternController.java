package com.pms.pattern_service.controllers;

import com.pms.pattern_service.dto.PatternRequest;
import com.pms.pattern_service.entities.Pattern;
import com.pms.pattern_service.services.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;

    @PostMapping
    public ResponseEntity<Pattern> createPattern(@RequestBody PatternRequest request) {
        return ResponseEntity.ok(patternService.createPattern(request));
    }

    @GetMapping
    public ResponseEntity<List<Pattern>> getAllPatterns() {
        return ResponseEntity.ok(patternService.getAllPatterns());
    }
}
