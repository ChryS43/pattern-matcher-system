package com.pms.keystroke_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "keystrokes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keystroke {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId; // Related to session microservice

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private Instant timestamp;

    private Long durationMs;
}
