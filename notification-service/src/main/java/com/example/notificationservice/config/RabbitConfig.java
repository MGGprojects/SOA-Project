package com.example.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "event.created.queue";

    @Bean
    public Queue eventCreatedQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}