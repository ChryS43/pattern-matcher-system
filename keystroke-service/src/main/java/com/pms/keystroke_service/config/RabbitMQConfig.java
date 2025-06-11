package com.pms.keystroke_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class RabbitMQConfig {
    @Value("${keystroke.queue.name}")
    private String queueName;

    private final RabbitAdmin rabbitAdmin;
    private Queue keystrokeQueue;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    @PostConstruct
    public void init() {
        this.keystrokeQueue = new Queue(queueName, true);
        rabbitAdmin.declareQueue(keystrokeQueue);
    }

    @Bean
    public Queue keystrokeQueue() {
        return keystrokeQueue;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
} 