package com.pms.match_event.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    @Value("${match-event.exchange.name}")
    private String exchangeName;

    @Value("${match-event.queue.name}")
    private String queueName;

    @Value("${match-event.routing.key}")
    private String routingKey;

    @Bean
    public DirectExchange patternMatchExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue matchEventQueue() {
        return new Queue(queueName, true);  // durable queue
    }

    @Bean
    public Binding binding(Queue matchEventQueue, DirectExchange patternMatchExchange) {
        return BindingBuilder
                .bind(matchEventQueue)
                .to(patternMatchExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
