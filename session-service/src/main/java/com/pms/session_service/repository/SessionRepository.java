package com.pms.session_service.repositories;

import com.pms.session_service.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByUuid(UUID uuid);
}
