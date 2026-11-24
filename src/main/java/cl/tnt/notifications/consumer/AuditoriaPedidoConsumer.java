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
 * Consumidor de auditoría. Dominio funcional: trazabilidad de eventos de pedidos.
 * Recibe todos los eventos "pedidos.#" del exchange topic; los que no cumplen la
 * integridad mínima van a pedidos.auditoria.dlq.
 */
@Component
public class AuditoriaPedidoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaPedidoConsumer.class);

    @RabbitListener(
            queues = "${app.rabbitmq.queues.auditoria}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void registrarAuditoria(
            @Payload PedidoEventDTO evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            if (evento == null || evento.getId() == null
                    || evento.getMonto() == null || evento.getMonto() <= 0) {
                log.warn("[AUDITORIA] Evento sin integridad mínima (id o monto inválido). Rechazando a DLQ: {}", evento);
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            log.info("[AUDITORIA] Evento registrado: tipo={}, pedidoId={}, cliente='{}', monto={}",
                    evento.getTipoEvento(), evento.getId(), evento.getCliente(), evento.getMonto());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[AUDITORIA] Error inesperado tag={}: {}. Enviando a DLQ.", deliveryTag, e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
