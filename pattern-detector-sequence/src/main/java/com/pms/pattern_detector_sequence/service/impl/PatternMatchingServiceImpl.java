package com.pms.pattern_detector_sequence.service.impl;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import com.pms.pattern_detector_sequence.evaluator.PatternEvaluator;
import com.pms.pattern_detector_sequence.event.PatternMatchEvent;
import com.pms.pattern_detector_sequence.repository.PatternRepository;
import com.pms.pattern_detector_sequence.service.KeystrokeBufferService;
import com.pms.pattern_detector_sequence.service.PatternMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatternMatchingServiceImpl implements PatternMatchingService {

    private final KeystrokeBufferService bufferService;
    private final PatternRepository patternRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private static final String PATTERN_MATCH_EXCHANGE = "pattern.match.exchange";
    private static final String PATTERN_MATCH_ROUTING_KEY = "pattern.match";

    @Override
    public void processKeystroke(String sessionId, KeystrokeMessage event) {
        log.info("Processing keystroke for session {}: key={}, timestamp={}, duration={}ms",
                sessionId, event.getKey(), event.getTimestamp(), event.getDurationMs());
        bufferService.addEvent(sessionId, event);
        log.info("Keystroke saved to buffer");
        evaluatePatterns(sessionId);
    }

    @Override
    public List<PatternEntity> getMatchingPatterns(String sessionId) {
        List<PatternEntity> patterns = patternRepository.findBySessionId(sessionId);
        log.debug("Retrieved {} patterns for session {}", patterns.size(), sessionId);
        return patterns;
    }

    @Override
    public void registerPattern(PatternEntity pattern) {
        log.debug("Registering pattern: id={}, name={}, sessionId={}", 
                pattern.getId(), pattern.getName(), pattern.getSessionId());
        patternRepository.save(pattern);
    }

    @Override
    public void unregisterPattern(String patternId) {
        log.debug("Unregistering pattern: id={}", patternId);
        patternRepository.deleteById(patternId);
    }

    private void evaluatePatterns(String sessionId) {
        log.info("Evaluating patterns for session {}", sessionId);
        
        // Get all events
        List<KeystrokeMessage> events = bufferService.getEvents(sessionId);
        log.info("Found {} events to evaluate", events.size());

        if (events.isEmpty()) {
            log.info("No events to evaluate for session {}", sessionId);
            return;
        }

        // Log the sequence of events being evaluated
        String eventSequence = events.stream()
                .map(KeystrokeMessage::getKey)
                .reduce((a, b) -> a + " -> " + b)
                .orElse("");
        log.info("Evaluating event sequence: {}", eventSequence);

        List<PatternEntity> patterns = patternRepository.findBySessionId(sessionId);
        if (patterns.isEmpty()) {
            log.debug("No patterns found for session {}", sessionId);
        }
        for (PatternEntity pattern : patterns) {
            evaluatePattern(pattern, events);
        }
        log.info("Pattern matching completed for session {}", sessionId);
    }

    private void evaluatePattern(PatternEntity pattern, List<KeystrokeMessage> events) {
        log.debug("Evaluating pattern: id={}, name={}", pattern.getId(), pattern.getName());
        long currentTime = System.currentTimeMillis();
        PatternEvaluator.EvaluationResult result = evaluateBlock(pattern.getRootBlock(), events, currentTime);
        
        log.debug("Pattern evaluation result: id={}, result={}", pattern.getId(), result);
        if (result == PatternEvaluator.EvaluationResult.MATCHED) {
            handlePatternMatch(pattern, events);
        }
    }

    private PatternEvaluator.EvaluationResult evaluateBlock(PatternEntity.PatternBlock block, 
            List<KeystrokeMessage> events, long currentTime) {
        if (block == null) {
            log.debug("Block is null, returning FAILED");
            return PatternEvaluator.EvaluationResult.FAILED;
        }

        // Check time window constraint if present
        if (block.getConstraints() != null && block.getConstraints().getWithinMs() != null) {
            long windowStart = currentTime - block.getConstraints().getWithinMs();
            if (events.isEmpty() || events.get(0).getTimestamp().toEpochMilli() < windowStart) {
                log.debug("Time window constraint violated: withinMs={}, windowStart={}", 
                        block.getConstraints().getWithinMs(), windowStart);
                return PatternEvaluator.EvaluationResult.FAILED;
            }
        }

        // Evaluate match condition
        if (block.getMatch() != null) {
            log.debug("Evaluating match condition: key={}, anyKey={}", 
                    block.getMatch().getKey(), block.getMatch().getAnyKey());
            return evaluateMatchCondition(block.getMatch(), events, currentTime);
        }

        // Evaluate sequence
        if (block.getSequence() != null && !block.getSequence().isEmpty()) {
            log.debug("Evaluating sequence with {} blocks", block.getSequence().size());
            return evaluateSequence(block.getSequence(), events, currentTime, block.getConstraints());
        }

        // Evaluate repeat
        if (block.getRepeat() != null) {
            log.debug("Evaluating repeat block: times={}", block.getRepeat().getTimes());
            return evaluateRepeat(block.getRepeat(), events, currentTime);
        }

        // Evaluate aggregate
        if (block.getAggregate() != null) {
            log.debug("Evaluating aggregate condition: withinMs={}", block.getAggregate().getWithinMs());
            return evaluateAggregate(block.getAggregate(), events, currentTime);
        }

        log.debug("No valid block type found, returning FAILED");
        return PatternEvaluator.EvaluationResult.FAILED;
    }

    private PatternEvaluator.EvaluationResult evaluateMatchCondition(
            PatternEntity.MatchCondition condition, List<KeystrokeMessage> events, long currentTime) {
        if (events.isEmpty()) {
            log.debug("No events to evaluate match condition, returning WAITING");
            return PatternEvaluator.EvaluationResult.WAITING;
        }

        KeystrokeMessage lastEvent = events.get(events.size() - 1);
        log.debug("Evaluating match condition against event: key={}, duration={}ms", 
                lastEvent.getKey(), lastEvent.getDurationMs());
        
        // Check key match
        if (!condition.getAnyKey() && !lastEvent.getKey().equals(condition.getKey())) {
            log.debug("Key mismatch: expected={}, actual={}", condition.getKey(), lastEvent.getKey());
            return PatternEvaluator.EvaluationResult.FAILED;
        }

        // Check press duration constraints
        if (condition.getPressDurationGt() != null && 
                lastEvent.getDurationMs() <= condition.getPressDurationGt()) {
            log.debug("Press duration too short: actual={}ms, required={}ms", 
                    lastEvent.getDurationMs(), condition.getPressDurationGt());
            return PatternEvaluator.EvaluationResult.FAILED;
        }
        if (condition.getPressDurationLt() != null && 
                lastEvent.getDurationMs() >= condition.getPressDurationLt()) {
            log.debug("Press duration too long: actual={}ms, max={}ms", 
                    lastEvent.getDurationMs(), condition.getPressDurationLt());
            return PatternEvaluator.EvaluationResult.FAILED;
        }

        log.debug("Match condition satisfied");
        return PatternEvaluator.EvaluationResult.MATCHED;
    }

    private PatternEvaluator.EvaluationResult evaluateSequence(
            List<PatternEntity.PatternBlock> sequence, List<KeystrokeMessage> events, 
            long currentTime, PatternEntity.Constraints constraints) {
        if (events.isEmpty()) {
            log.debug("No events to evaluate sequence, returning WAITING");
            return PatternEvaluator.EvaluationResult.WAITING;
        }

        int eventIndex = 0;
        KeystrokeMessage lastMatchedEvent = null;

        for (PatternEntity.PatternBlock block : sequence) {
            // Check if we have enough events left
            if (eventIndex >= events.size()) {
                log.debug("Not enough events to complete sequence, returning WAITING");
                return PatternEvaluator.EvaluationResult.WAITING;
            }

            // Check gap constraints between consecutive events
            if (lastMatchedEvent != null && constraints != null) {
                KeystrokeMessage currentEvent = events.get(eventIndex);
                long gap = Duration.between(lastMatchedEvent.getTimestamp(), currentEvent.getTimestamp()).toMillis();
                log.debug("Checking gap constraints: gap={}ms, gapGt={}, gapLt={}", 
                        gap, constraints.getGapGt(), constraints.getGapLt());

                if (constraints.getGapGt() != null && gap <= constraints.getGapGt()) {
                    log.debug("Gap too small, returning WAITING");
                    return PatternEvaluator.EvaluationResult.WAITING;
                }
                if (constraints.getGapLt() != null && gap >= constraints.getGapLt()) {
                    log.debug("Gap too large, returning FAILED");
                    return PatternEvaluator.EvaluationResult.FAILED;
                }
            }

            // Try to match the current block with the next event
            log.debug("Evaluating sequence block at index {}", eventIndex);
            PatternEvaluator.EvaluationResult result = evaluateBlock(block, 
                    events.subList(eventIndex, events.size()), currentTime);
            
            if (result == PatternEvaluator.EvaluationResult.FAILED) {
                log.debug("Sequence block evaluation failed");
                return PatternEvaluator.EvaluationResult.FAILED;
            }
            if (result == PatternEvaluator.EvaluationResult.WAITING) {
                log.debug("Sequence block evaluation waiting");
                return PatternEvaluator.EvaluationResult.WAITING;
            }

            // Block matched, advance to next event and block
            lastMatchedEvent = events.get(eventIndex);
            eventIndex++;
            log.debug("Sequence block matched, advancing to next block");
        }

        log.debug("Sequence evaluation completed successfully");
        return PatternEvaluator.EvaluationResult.MATCHED;
    }

    private PatternEvaluator.EvaluationResult evaluateRepeat(
            PatternEntity.RepeatBlock repeat, List<KeystrokeMessage> events, long currentTime) {
        if (events.isEmpty()) {
            log.debug("No events to evaluate repeat block, returning WAITING");
            return PatternEvaluator.EvaluationResult.WAITING;
        }

        int matchCount = 0;
        int eventIndex = 0;
        KeystrokeMessage lastMatchedEvent = null;

        while (matchCount < repeat.getTimes() && eventIndex < events.size()) {
            // Check gap constraints if present
            if (lastMatchedEvent != null && repeat.getBlock().getConstraints() != null) {
                KeystrokeMessage currentEvent = events.get(eventIndex);
                long gap = Duration.between(lastMatchedEvent.getTimestamp(), currentEvent.getTimestamp()).toMillis();
                log.debug("Checking repeat gap constraints: gap={}ms, gapGt={}, gapLt={}", 
                        gap, repeat.getBlock().getConstraints().getGapGt(), 
                        repeat.getBlock().getConstraints().getGapLt());

                if (repeat.getBlock().getConstraints().getGapGt() != null && 
                        gap <= repeat.getBlock().getConstraints().getGapGt()) {
                    log.debug("Repeat gap too small, returning WAITING");
                    return PatternEvaluator.EvaluationResult.WAITING;
                }
                if (repeat.getBlock().getConstraints().getGapLt() != null && 
                        gap >= repeat.getBlock().getConstraints().getGapLt()) {
                    log.debug("Repeat gap too large, returning FAILED");
                    return PatternEvaluator.EvaluationResult.FAILED;
                }
            }

            log.debug("Evaluating repeat block iteration {}/{}", matchCount + 1, repeat.getTimes());
            PatternEvaluator.EvaluationResult result = evaluateBlock(repeat.getBlock(),
                    events.subList(eventIndex, events.size()), currentTime);
            
            if (result == PatternEvaluator.EvaluationResult.FAILED) {
                log.debug("Repeat block evaluation failed");
                return PatternEvaluator.EvaluationResult.FAILED;
            }
            if (result == PatternEvaluator.EvaluationResult.WAITING) {
                log.debug("Repeat block evaluation waiting");
                return PatternEvaluator.EvaluationResult.WAITING;
            }
            
            matchCount++;
            lastMatchedEvent = events.get(eventIndex);
            eventIndex++;
            log.debug("Repeat block iteration matched, advancing to next iteration");
        }

        boolean matched = matchCount == repeat.getTimes();
        log.debug("Repeat block evaluation completed: matched={}, count={}/{}", 
                matched, matchCount, repeat.getTimes());
        return matched ? 
                PatternEvaluator.EvaluationResult.MATCHED : 
                PatternEvaluator.EvaluationResult.WAITING;
    }

    private PatternEvaluator.EvaluationResult evaluateAggregate(
            PatternEntity.AggregateCondition aggregate, List<KeystrokeMessage> events, long currentTime) {
        if (events.isEmpty()) {
            log.debug("No events to evaluate aggregate condition, returning WAITING");
            return PatternEvaluator.EvaluationResult.WAITING;
        }

        // Check time window constraint
        if (aggregate.getWithinMs() != null) {
            long windowStart = currentTime - aggregate.getWithinMs();
            events = events.stream()
                    .filter(e -> e.getTimestamp().toEpochMilli() >= windowStart)
                    .toList();

            if (events.isEmpty()) {
                log.debug("No events within time window {}ms, returning FAILED", aggregate.getWithinMs());
                return PatternEvaluator.EvaluationResult.FAILED;
            }
            log.debug("Filtered to {} events within time window {}ms", events.size(), aggregate.getWithinMs());
        }

        int count = events.size();
        log.debug("Evaluating aggregate condition: count={}, countEq={}, countGte={}, countLte={}", 
                count, aggregate.getCountEq(), aggregate.getCountGte(), aggregate.getCountLte());

        // Check count constraints
        if (aggregate.getCountEq() != null && count != aggregate.getCountEq()) {
            log.debug("Count mismatch: actual={}, expected={}", count, aggregate.getCountEq());
            return count < aggregate.getCountEq() ? 
                    PatternEvaluator.EvaluationResult.WAITING : 
                    PatternEvaluator.EvaluationResult.FAILED;
        }
        if (aggregate.getCountGte() != null && count < aggregate.getCountGte()) {
            log.debug("Count too low: actual={}, minimum={}", count, aggregate.getCountGte());
            return PatternEvaluator.EvaluationResult.WAITING;
        }
        if (aggregate.getCountLte() != null && count > aggregate.getCountLte()) {
            log.debug("Count too high: actual={}, maximum={}", count, aggregate.getCountLte());
            return PatternEvaluator.EvaluationResult.FAILED;
        }

        log.debug("Aggregate condition satisfied");
        return PatternEvaluator.EvaluationResult.MATCHED;
    }

    private void handlePatternMatch(PatternEntity pattern, List<KeystrokeMessage> matchedEvents) {
        log.debug("Pattern matched: id={}, name={}, sessionId={}, matchedEvents={}", 
                pattern.getId(), pattern.getName(), pattern.getSessionId(), matchedEvents.size());

        // Commented out event publishing for now
        /*
        PatternMatchEvent matchEvent = new PatternMatchEvent(
                pattern.getId(),
                pattern.getName(),
                pattern.getSessionId(),
                Instant.now(),
                matchedEvents.stream().map(KeystrokeMessage::getId).map(Object::toString).toList()
        );

        try {
            // Publish to RabbitMQ
            log.debug("Publishing pattern match to RabbitMQ: exchange={}, routingKey={}", 
                    PATTERN_MATCH_EXCHANGE, PATTERN_MATCH_ROUTING_KEY);
            rabbitTemplate.convertAndSend(PATTERN_MATCH_EXCHANGE, PATTERN_MATCH_ROUTING_KEY, matchEvent);
            
            // Publish as Spring event
            log.debug("Publishing pattern match as Spring event");
            eventPublisher.publishEvent(matchEvent);
        } catch (Exception e) {
            log.error("Failed to publish pattern match event: {}", e.getMessage(), e);
            // Fallback to logging if event publishing fails
            System.out.println("Pattern matched: " + pattern.getName() + 
                    " (Session: " + pattern.getSessionId() + ")");
        }
        */

        // Simple logging of matched key and duration
        if (!matchedEvents.isEmpty()) {
            KeystrokeMessage lastEvent = matchedEvents.get(matchedEvents.size() - 1);
            log.info("Pattern matched: key={}, duration={}ms", 
                    lastEvent.getKey(), lastEvent.getDurationMs());
        }
    }
} 