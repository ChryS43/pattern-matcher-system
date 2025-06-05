package com.pms.session_service.service;

import com.pms.session_service.dto.SessionDto;

import java.util.UUID;

public interface SessionService {
    SessionDto createSession();
    SessionDto getSession(UUID uuid);
    SessionDto closeSession(UUID uuid);
}
