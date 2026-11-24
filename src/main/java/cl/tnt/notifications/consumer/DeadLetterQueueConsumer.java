package cl.tnt.notifications.consumer;

import cl.tnt.notifications.dto.PedidoEventDTO;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Registra en logs los mensajes no entregados de las 3 DLQ.
 * Para ver los mensajes acumulados en la consola de RabbitMQ (demo), apagar este consumidor con
 * app.rabbitmq.dlq-consumer.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.rabbitmq.dlq-consumer.enabled", havingValue = "true", matchIfMissing = true)
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    @RabbitListener(
            queues = {
                    "${app.rabbitmq.dlqs.notificacion}",
                    "${app.rabbitmq.dlqs.ticket}",
                    "${app.rabbitmq.dlqs.auditoria}"
            },
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarMensajeFallidoDLQ(
            @Payload PedidoEventDTO eventoFallido,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(AmqpHeaders.CONSUMER_QUEUE) String colaDlq,
            @Header(name = "x-death", required = false) Object xDeath
    ) throws IOException {

        log.warn("[DLQ] Mensaje no entregado capturado en '{}'. Pedido ID={}. Contenido: {}",
                colaDlq, eventoFallido != null ? eventoFallido.getId() : "DESCONOCIDO", eventoFallido);
        log.warn("[DLQ] Historial de rechazo (x-death): {}", xDeath);

        channel.basicAck(deliveryTag, false);
    }
}
