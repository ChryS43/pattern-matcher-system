package com.pms.session_service.controllers;

import com.pms.session_service.dto.CreateSessionRequest;
import com.pms.session_service.dto.SessionDto;
import com.pms.session_service.service.SessionService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public SessionDto createSession(@Parameter(hidden = true) @RequestBody(required = false) CreateSessionRequest request) {
        return sessionService.createSession();
    }

    @GetMapping("/{uuid}")
    public SessionDto getSession(@PathVariable UUID uuid) {
        return sessionService.getSession(uuid);
    }

    @PutMapping("/{uuid}/close")
    public SessionDto closeSession(@PathVariable UUID uuid) {
        return sessionService.closeSession(uuid);
    }
}
