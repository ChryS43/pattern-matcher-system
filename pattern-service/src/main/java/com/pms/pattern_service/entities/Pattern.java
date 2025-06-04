package com.pms.pattern_service.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "patterns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pattern {
    @Id
    private String id;
    private String name;
    private String type;
    private Map<String, Object> config;
}
