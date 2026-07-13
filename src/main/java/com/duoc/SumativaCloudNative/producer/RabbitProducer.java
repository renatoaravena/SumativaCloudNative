package com.duoc.SumativaCloudNative.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.duoc.SumativaCloudNative.constants.RabbitConstants;
import com.duoc.SumativaCloudNative.dto.GuiaMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviarGuia(GuiaMessage guiaMessage) {

        log.info("Enviando guía a RabbitMQ: {}", guiaMessage);

        rabbitTemplate.convertAndSend(
                RabbitConstants.GUIA_EXCHANGE,
                RabbitConstants.GUIA_ROUTING_KEY,
                guiaMessage);

        log.info("Mensaje enviado correctamente.");
    }

}