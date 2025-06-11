package com.pms.keystroke_service.service.impl;

import com.pms.keystroke_service.dto.KeystrokeRequest;
import com.pms.keystroke_service.dto.KeystrokeResponse;
import com.pms.keystroke_service.entity.Keystroke;
import com.pms.keystroke_service.repository.KeystrokeRepository;
import com.pms.keystroke_service.service.KeystrokeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeystrokeServiceImpl implements KeystrokeService {

    private final KeystrokeRepository repository;

    @Override
    public KeystrokeResponse save(KeystrokeRequest request) {
        Keystroke k = Keystroke.builder()
                .sessionId(request.getSessionId())
                .key(request.getKey())
                .timestamp(Instant.now())
                .durationMs(request.getDurationMs())
                .build();

        return toResponse(repository.save(k));
    }

    @Override
    public List<KeystrokeResponse> getBySession(UUID sessionId) {
        return repository.findBySessionId(sessionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KeystrokeResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Keystroke not found with id: " + id));
    }

    private KeystrokeResponse toResponse(Keystroke k) {
        return KeystrokeResponse.builder()
                .id(k.getId())
                .sessionId(k.getSessionId())
                .key(k.getKey())
                .timestamp(k.getTimestamp())
                .durationMs(k.getDurationMs())
                .build();
    }
}
