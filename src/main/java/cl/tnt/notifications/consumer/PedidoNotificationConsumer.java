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
 * Consumidor de Notificaciones de Pedidos.
 * Dominio Funcional: Notificaciones al Cliente y Alertas.
 *
 * Cumple con los Indicadores 4 y 5 de la Pauta:
 * - Implementado con @RabbitListener y agrupado en paquete de dominio funcional.
 * - Confirmación manual explícita (ACK) mediante Channel.basicAck.
 * - Manejo explícito de errores diferenciando casos de negocio y desvío a DLQ con Channel.basicNack(..., requeue=false).
 */
@Component
public class PedidoNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoNotificationConsumer.class);

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarNotificacionPedido(
            @Payload PedidoEventDTO evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info("================================================================================");
        log.info("[DOMINIO: NOTIFICACIONES] Recibido evento de pedido para procesamiento asíncrono.");
        log.info("Delivery Tag: {}, Evento: {}", deliveryTag, evento);

        try {
            // 1. Validación de reglas de negocio para control de fallos
            if (evento == null) {
                log.error("[ERROR NO RECUPERABLE] Payload de evento nulo. Rechazando a DLQ.");
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            if (evento.getMonto() != null && evento.getMonto() <= 0) {
                // Caso de prueba para demostrar envío a DLQ por datos inválidos de negocio
                log.warn("[VALIDACIÓN FALLIDA] El pedido ID={} tiene un monto inválido: ${}. Rechazando hacia Dead Letter Queue (DLQ).",
                        evento.getId(), evento.getMonto());
                // basicNack con requeue = false desvía automáticamente el mensaje a la DLQ configurada en el exchange
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // 2. Simulación de envío de notificación asíncrona (Email / SMS / Push)
            log.info("[NOTIFICACIÓN ENVIADA] Notificando al cliente '{}' sobre su pedido de '{}' por un total de ${}.",
                    evento.getCliente(), evento.getProducto(), evento.getMonto());

            // 3. Confirmación manual explícita (ACK)
            channel.basicAck(deliveryTag, false);
            log.info("[ACK CONFIRMADO] Mensaje procesado exitosamente con tag={}", deliveryTag);
            log.info("================================================================================");

        } catch (IllegalArgumentException e) {
            // Error en formato o contenido del mensaje (no recuperable -> directo a DLQ)
            log.error("[ERROR IRRECUPERABLE] Error de validación en mensaje tag={}. Enviando a DLQ: {}", deliveryTag, e.getMessage());
            channel.basicNack(deliveryTag, false, false);

        } catch (Exception e) {
            // Error genérico imprevisto
            log.error("[ERROR INESPERADO] Excepción procesando notificación tag={}: {}", deliveryTag, e.getMessage(), e);
            // NACK sin reencolamiento para evitar bucles infinitos de fallo; pasa a la DLQ para auditoría
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
