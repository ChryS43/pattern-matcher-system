package com.pms.pattern_detector_sequence.service;

import com.pms.pattern_detector_sequence.dto.KeystrokeMessage;
import com.pms.pattern_detector_sequence.entity.PatternEntity;
import com.pms.pattern_detector_sequence.repository.PatternRepository;
import com.pms.pattern_detector_sequence.service.impl.PatternMatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatternMatchingServiceImplTest {

    @Mock
    private KeystrokeBufferService bufferService;
    @Mock
    private PatternRepository patternRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<List<KeystrokeMessage>> eventsCaptor;

    private PatternMatchingServiceImpl patternMatchingService;
    private String sessionId;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        patternMatchingService = new PatternMatchingServiceImpl(
                bufferService, patternRepository, rabbitTemplate, eventPublisher);
        sessionId = UUID.randomUUID().toString();
        baseTime = Instant.now();
    }

    // Helper methods to create test data
    private KeystrokeMessage createKeystroke(String key, long timeOffset, long duration) {
        KeystrokeMessage message = new KeystrokeMessage();
        message.setId(UUID.randomUUID());
        message.setSessionId(UUID.fromString(sessionId));
        message.setKey(key);
        message.setTimestamp(baseTime.plusMillis(timeOffset));
        message.setDurationMs(duration);
        return message;
    }

    private PatternEntity.PatternBlock createMatchBlock(String key, Long pressDurationGt, Long pressDurationLt) {
        PatternEntity.MatchCondition match = new PatternEntity.MatchCondition();
        match.setKey(key);
        match.setAnyKey(false);
        match.setPressDurationGt(pressDurationGt);
        match.setPressDurationLt(pressDurationLt);
        
        PatternEntity.PatternBlock block = new PatternEntity.PatternBlock();
        block.setMatch(match);
        return block;
    }

    private PatternEntity.PatternBlock createSequenceBlock(List<PatternEntity.PatternBlock> blocks, 
            Long gapGt, Long gapLt) {
        PatternEntity.Constraints constraints = new PatternEntity.Constraints();
        constraints.setGapGt(gapGt);
        constraints.setGapLt(gapLt);
        
        PatternEntity.PatternBlock block = new PatternEntity.PatternBlock();
        block.setSequence(blocks);
        block.setConstraints(constraints);
        return block;
    }

    private PatternEntity.PatternBlock createRepeatBlock(PatternEntity.PatternBlock block, int times) {
        PatternEntity.RepeatBlock repeat = new PatternEntity.RepeatBlock();
        repeat.setBlock(block);
        repeat.setTimes(times);
        
        PatternEntity.PatternBlock repeatBlock = new PatternEntity.PatternBlock();
        repeatBlock.setRepeat(repeat);
        return repeatBlock;
    }

    private PatternEntity.PatternBlock createAggregateBlock(Integer countEq, Integer countGte, 
            Integer countLte, Long withinMs) {
        PatternEntity.AggregateCondition aggregate = new PatternEntity.AggregateCondition();
        aggregate.setCountEq(countEq);
        aggregate.setCountGte(countGte);
        aggregate.setCountLte(countLte);
        aggregate.setWithinMs(withinMs);
        
        PatternEntity.PatternBlock block = new PatternEntity.PatternBlock();
        block.setAggregate(aggregate);
        return block;
    }

    // Test cases for basic pattern matching
    @Test
    void testSimpleKeyMatch() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Simple Key Match");
        pattern.setSessionId(sessionId);
        pattern.setRootBlock(createMatchBlock("a", null, null));

        KeystrokeMessage event = createKeystroke("a", 0, 100);
        when(bufferService.getEvents(sessionId)).thenReturn(List.of(event));
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        patternMatchingService.processKeystroke(sessionId, event);

        // Assert
        verify(bufferService).addEvent(sessionId, event);
        verify(patternRepository).findBySessionId(sessionId);
    }

    @Test
    void testKeyMatchWithDurationConstraints() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Key Match with Duration");
        pattern.setSessionId(sessionId);
        pattern.setRootBlock(createMatchBlock("a", 50L, 150L));

        KeystrokeMessage event = createKeystroke("a", 0, 100);
        when(bufferService.getEvents(sessionId)).thenReturn(List.of(event));
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        patternMatchingService.processKeystroke(sessionId, event);

        // Assert
        verify(bufferService).addEvent(sessionId, event);
        verify(patternRepository).findBySessionId(sessionId);
    }

    @ParameterizedTest
    @CsvSource({
        "a, 0, 100, true",    // Valid key and duration
        "b, 0, 100, false",   // Wrong key
        "a, 0, 40, false",    // Duration too short
        "a, 0, 160, false"    // Duration too long
    })
    void testKeyMatchWithVariousInputs(String key, long timeOffset, long duration, boolean shouldMatch) {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Key Match Variations");
        pattern.setSessionId(sessionId);
        pattern.setRootBlock(createMatchBlock("a", 50L, 150L));

        KeystrokeMessage event = createKeystroke(key, timeOffset, duration);
        when(bufferService.getEvents(sessionId)).thenReturn(List.of(event));
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        patternMatchingService.processKeystroke(sessionId, event);

        // Assert
        verify(bufferService).addEvent(sessionId, event);
        verify(patternRepository).findBySessionId(sessionId);
    }

    // Test cases for sequence matching
    @Test
    void testSimpleSequenceMatch() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Simple Sequence");
        pattern.setSessionId(sessionId);
        
        List<PatternEntity.PatternBlock> sequence = List.of(
            createMatchBlock("a", null, null),
            createMatchBlock("b", null, null),
            createMatchBlock("c", null, null)
        );
        pattern.setRootBlock(createSequenceBlock(sequence, null, null));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 100),
            createKeystroke("b", 100, 100),
            createKeystroke("c", 200, 100)
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(3)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(3)).findBySessionId(sessionId);
    }

    @Test
    void testSequenceWithGapConstraints() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Sequence with Gaps");
        pattern.setSessionId(sessionId);
        
        List<PatternEntity.PatternBlock> sequence = List.of(
            createMatchBlock("a", null, null),
            createMatchBlock("b", null, null)
        );
        pattern.setRootBlock(createSequenceBlock(sequence, 50L, 150L));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 100),
            createKeystroke("b", 100, 100)  // 100ms gap, within constraints
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(2)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(2)).findBySessionId(sessionId);
    }

    // Test cases for repeat blocks
    @Test
    void testRepeatBlock() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Repeat Block");
        pattern.setSessionId(sessionId);
        
        PatternEntity.PatternBlock matchBlock = createMatchBlock("a", null, null);
        pattern.setRootBlock(createRepeatBlock(matchBlock, 3));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 100),
            createKeystroke("a", 100, 100),
            createKeystroke("a", 200, 100)
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(3)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(3)).findBySessionId(sessionId);
    }

    // Test cases for aggregate conditions
    @Test
    void testAggregateCondition() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Aggregate Condition");
        pattern.setSessionId(sessionId);
        pattern.setRootBlock(createAggregateBlock(3, null, null, 1000L));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 100),
            createKeystroke("b", 100, 100),
            createKeystroke("c", 200, 100)
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(3)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(3)).findBySessionId(sessionId);
    }

    // Test cases for complex patterns
    @Test
    void testComplexPattern() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Complex Pattern");
        pattern.setSessionId(sessionId);
        
        // Create a complex pattern: (a -> b) * 2 -> c
        PatternEntity.PatternBlock innerSequence = createSequenceBlock(
            List.of(
                createMatchBlock("a", null, null),
                createMatchBlock("b", null, null)
            ),
            50L, 150L
        );
        
        PatternEntity.PatternBlock repeatBlock = createRepeatBlock(innerSequence, 2);
        
        List<PatternEntity.PatternBlock> outerSequence = List.of(
            repeatBlock,
            createMatchBlock("c", null, null)
        );
        
        pattern.setRootBlock(createSequenceBlock(outerSequence, null, null));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 100),
            createKeystroke("b", 100, 100),
            createKeystroke("a", 200, 100),
            createKeystroke("b", 300, 100),
            createKeystroke("c", 400, 100)
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(5)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(5)).findBySessionId(sessionId);
    }

    // Test cases for boundary conditions
    @Test
    void testBoundaryConditions() {
        // Arrange
        PatternEntity pattern = new PatternEntity();
        pattern.setId("test-pattern");
        pattern.setName("Boundary Conditions");
        pattern.setSessionId(sessionId);
        
        PatternEntity.PatternBlock matchBlock = createMatchBlock("a", 50L, 150L);
        PatternEntity.Constraints constraints = new PatternEntity.Constraints();
        constraints.setWithinMs(1000L);
        matchBlock.setConstraints(constraints);
        pattern.setRootBlock(matchBlock);

        // Test cases with various timestamps
        List<KeystrokeMessage> events = List.of(
            createKeystroke("a", 0, 49),    // Duration too short
            createKeystroke("a", 0, 151),   // Duration too long
            createKeystroke("a", 0, 100),   // Valid duration
            createKeystroke("a", 1000, 100) // Outside time window
        );
        
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        // Act
        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        // Assert
        verify(bufferService, times(4)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(4)).findBySessionId(sessionId);
    }

    @Test
    void testBiometricPatternForAuthentication() {
        // Immagina un pattern di battitura dell'utente reale:
        // Esempio: "p" -> "a" -> "s" -> "s" con tempi e gap tipici.
        PatternEntity pattern = new PatternEntity();
        pattern.setId("biometric-pattern");
        pattern.setName("Biometric Auth");
        pattern.setSessionId(sessionId);

        List<PatternEntity.PatternBlock> sequence = List.of(
            createMatchBlock("p", null, null),
            createMatchBlock("a", null, null),
            createMatchBlock("s", null, null),
            createMatchBlock("s", null, null)
        );
        pattern.setRootBlock(createSequenceBlock(sequence, 50L, 200L)); // gap tipico tra tasti

        List<KeystrokeMessage> events = List.of(
            createKeystroke("p", 0, 80),
            createKeystroke("a", 100, 90),
            createKeystroke("s", 200, 85),
            createKeystroke("s", 300, 80)
        );
        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        verify(bufferService, times(4)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(4)).findBySessionId(sessionId);
    }

    @Test
    void testBotDetectionPattern() {
        // Bot: pressione dei tasti con intervalli troppo regolari, sospetti.
        PatternEntity pattern = new PatternEntity();
        pattern.setId("bot-detection");
        pattern.setName("Bot Detection");
        pattern.setSessionId(sessionId);

        PatternEntity.AggregateCondition aggregate = new PatternEntity.AggregateCondition();
        aggregate.setCountEq(10); // esempio: 10 tasti in < 500ms → troppo veloce per un umano
        aggregate.setWithinMs(500L);

        PatternEntity.PatternBlock block = new PatternEntity.PatternBlock();
        block.setAggregate(aggregate);
        pattern.setRootBlock(block);

        List<KeystrokeMessage> events = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(createKeystroke("a", i * 50, 20));
        }

        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        verify(bufferService, times(10)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(10)).findBySessionId(sessionId);
    }

    @Test
    void testSecretCommandPattern() {
        // Esempio: una combinazione segreta di tasti per sbloccare un comando
        // "up" -> "up" -> "down" -> "down" -> "left" -> "right" -> "left" -> "right" -> "b" -> "a"
        PatternEntity pattern = new PatternEntity();
        pattern.setId("konami-code");
        pattern.setName("Secret Command");
        pattern.setSessionId(sessionId);

        List<PatternEntity.PatternBlock> sequence = List.of(
            createMatchBlock("up", null, null),
            createMatchBlock("up", null, null),
            createMatchBlock("down", null, null),
            createMatchBlock("down", null, null),
            createMatchBlock("left", null, null),
            createMatchBlock("right", null, null),
            createMatchBlock("left", null, null),
            createMatchBlock("right", null, null),
            createMatchBlock("b", null, null),
            createMatchBlock("a", null, null)
        );
        pattern.setRootBlock(createSequenceBlock(sequence, 50L, 300L));

        List<KeystrokeMessage> events = List.of(
            createKeystroke("up", 0, 80),
            createKeystroke("up", 100, 80),
            createKeystroke("down", 200, 80),
            createKeystroke("down", 300, 80),
            createKeystroke("left", 400, 80),
            createKeystroke("right", 500, 80),
            createKeystroke("left", 600, 80),
            createKeystroke("right", 700, 80),
            createKeystroke("b", 800, 80),
            createKeystroke("a", 900, 80)
        );

        when(bufferService.getEvents(sessionId)).thenReturn(events);
        when(patternRepository.findBySessionId(sessionId)).thenReturn(List.of(pattern));

        events.forEach(event -> patternMatchingService.processKeystroke(sessionId, event));

        verify(bufferService, times(10)).addEvent(eq(sessionId), any(KeystrokeMessage.class));
        verify(patternRepository, times(10)).findBySessionId(sessionId);
    }

} 