package com.pms.pattern_detector_sequence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "patterns")
public class PatternEntity {

    @Id
    private String id;
    private String name;
    private String sessionId;
    private PatternBlock rootBlock;

    @Data
    @NoArgsConstructor
    public static class PatternBlock {
        private MatchCondition match;
        private List<PatternBlock> sequence;
        private RepeatBlock repeat;
        private AggregateCondition aggregate;
        private Constraints constraints;
    }

    @Data
    @NoArgsConstructor
    public static class MatchCondition {
        private String key;
        private Boolean anyKey;
        private Long pressDurationGt;
        private Long pressDurationLt;
    }

    @Data
    @NoArgsConstructor
    public static class RepeatBlock {
        private int times;
        private PatternBlock block;
    }

    @Data
    @NoArgsConstructor
    public static class AggregateCondition {
        private Integer countEq;
        private Integer countGte;
        private Integer countLte;
        private Long withinMs;
    }

    @Data
    @NoArgsConstructor
    public static class Constraints {
        private Boolean afterPrevious;
        private Long withinMs;
        private Long gapGt;
        private Long gapLt;
    }
}
