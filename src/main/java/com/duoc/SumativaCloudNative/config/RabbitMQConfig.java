package com.duoc.SumativaCloudNative.config;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.SumativaCloudNative.constants.RabbitConstants;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    CachingConnectionFactory connectionFactory() {

        CachingConnectionFactory factory = new CachingConnectionFactory();

        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        return factory;
    }

    @Bean
    Queue guiaQueue() {

        return new Queue(
                RabbitConstants.GUIA_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RabbitConstants.GUIA_DLX,
                        "x-dead-letter-routing-key", RabbitConstants.GUIA_DLX_ROUTING_KEY
                ));
    }

    @Bean
    Queue guiaDLQ() {
        return new Queue(RabbitConstants.GUIA_DLQ);
    }

    @Bean
    DirectExchange guiaExchange() {
        return new DirectExchange(RabbitConstants.GUIA_EXCHANGE);
    }

    @Bean
    DirectExchange guiaDLX() {
        return new DirectExchange(RabbitConstants.GUIA_DLX);
    }

    @Bean
    Binding guiaBinding() {

        return BindingBuilder
                .bind(guiaQueue())
                .to(guiaExchange())
                .with(RabbitConstants.GUIA_ROUTING_KEY);
    }

    @Bean
    Binding dlqBinding() {

        return BindingBuilder
                .bind(guiaDLQ())
                .to(guiaDLX())
                .with(RabbitConstants.GUIA_DLX_ROUTING_KEY);
    }

}