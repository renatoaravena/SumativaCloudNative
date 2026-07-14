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
        log.info("Evaluando guía antes de enviar a RabbitMQ: {}", guiaMessage);

        // Convertimos el objeto a un String en minúsculas para atrapar la palabra en cualquier campo
        String contenidoMensaje = String.valueOf(guiaMessage).toLowerCase();

        if (contenidoMensaje.contains("error")) {
            log.warn("Palabra 'error' detectada. Desviando mensaje directamente a la DLQ...");
            
            rabbitTemplate.convertAndSend(
                RabbitConstants.GUIA_DLX, 
                RabbitConstants.GUIA_DLX_ROUTING_KEY, 
                guiaMessage
            );
            
            log.info("Mensaje enviado exitosamente a la cola de errores (DLQ).");
        } else {
            log.info("Flujo normal. Enviando guía a la cola principal...");
            
            rabbitTemplate.convertAndSend(
                RabbitConstants.GUIA_EXCHANGE,
                RabbitConstants.GUIA_ROUTING_KEY,
                guiaMessage
            );
            
            log.info("Mensaje enviado correctamente a la cola principal.");
        }
    }
}