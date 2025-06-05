package com.pms.session_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
