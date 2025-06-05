package com.pms.session_service.service.impl;

import com.pms.session_service.dto.SessionDto;
import com.pms.session_service.entities.Session;
import com.pms.session_service.repositories.SessionRepository;
import com.pms.session_service.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Override
    public SessionDto createSession() {
        Session session = sessionRepository.save(
                Session.builder()
                        .uuid(UUID.randomUUID())
                        .build()
        );
        return toDto(session);
    }

    @Override
    public SessionDto getSession(UUID uuid) {
        return sessionRepository.findByUuid(uuid)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    @Override
    public SessionDto closeSession(UUID uuid) {
        Session session = sessionRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setClosedAt(LocalDateTime.now());
        return toDto(sessionRepository.save(session));
    }

    private SessionDto toDto(Session session) {
        return SessionDto.builder()
                .uuid(session.getUuid())
                .createdAt(session.getCreatedAt())
                .closedAt(session.getClosedAt())
                .build();
    }
}
