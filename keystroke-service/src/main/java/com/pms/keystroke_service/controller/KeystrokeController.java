package com.pms.keystroke_service.controller;

import com.pms.keystroke_service.dto.KeystrokeRequest;
import com.pms.keystroke_service.dto.KeystrokeResponse;
import com.pms.keystroke_service.service.KeystrokeService;
import com.pms.keystroke_service.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/keystrokes")
@RequiredArgsConstructor
public class KeystrokeController {

    private final KeystrokeService keystrokeService;
    private final RabbitMQService rabbitMQService;

    @PostMapping
    public ResponseEntity<KeystrokeResponse> saveKeystroke(@RequestBody KeystrokeRequest request) {
        KeystrokeResponse response = keystrokeService.save(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<KeystrokeResponse>> getKeystrokesBySession(@PathVariable UUID sessionId) {
        List<KeystrokeResponse> keystrokes = keystrokeService.getBySession(sessionId);
        return ResponseEntity.ok(keystrokes);
    }

    @PostMapping("/{id}/emit")
    public ResponseEntity<Void> emitKeystroke(@PathVariable UUID id) {
        rabbitMQService.emitKeystroke(id);
        return ResponseEntity.ok().build();
    }
}
