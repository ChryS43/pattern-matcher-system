package com.pms.pattern_service.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatternRequest {
    private String name;
    private String type;
    private Map<String, Object> config;
}
