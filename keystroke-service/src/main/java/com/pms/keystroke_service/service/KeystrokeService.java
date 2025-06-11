package com.pms.keystroke_service.service;

import com.pms.keystroke_service.dto.KeystrokeRequest;
import com.pms.keystroke_service.dto.KeystrokeResponse;

import java.util.List;
import java.util.UUID;

public interface KeystrokeService {
    KeystrokeResponse save(KeystrokeRequest request);
    List<KeystrokeResponse> getBySession(UUID sessionId);
    KeystrokeResponse getById(UUID id);
}
