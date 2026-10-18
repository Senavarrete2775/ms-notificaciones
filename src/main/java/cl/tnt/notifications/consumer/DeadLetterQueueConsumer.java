package cl.tnt.notifications.consumer;

import cl.tnt.notifications.dto.PedidoEventDTO;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de la Dead Letter Queue (DLQ).
 * Dominio Funcional: Auditoría de Mensajes Fallidos y Resiliencia.
 *
 * Cumple con la instrucción específica:
 * "Los microservicios deben manejar mensajes no entregados enviándolos a DLQ y registrándolos en logs."
 */
@Component
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    @RabbitListener(
            queues = "${app.rabbitmq.dlq}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarMensajeFallidoDLQ(
            @Payload PedidoEventDTO eventoFallido,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.warn("********************************************************************************");
        log.warn("[DEAD LETTER QUEUE (DLQ)] Mensaje fallido capturado en cola de mensajes no entregados.");
        log.warn("ID Pedido Fallido: {}", eventoFallido != null ? eventoFallido.getId() : "DESCONOCIDO");
        log.warn("Detalle del contenido rechazado: {}", eventoFallido);
        log.warn("Razón: Rechazo explícito en consumidor principal (basicNack con requeue=false).");
        log.warn("Acción: Registrado en log de auditoría para análisis de soporte o reintento administrativo.");
        log.warn("********************************************************************************");

        // Confirmamos la recepción en la DLQ para que no quede atascado eternamente
        channel.basicAck(deliveryTag, false);
    }
}
