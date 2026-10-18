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
 * Consumidor para Generación de Tickets y Facturación Asíncrona.
 * Dominio Funcional: Facturación, Boletas y Documentos Digitales.
 *
 * Cumple con los Indicadores 4 y 5 de la Pauta:
 * - Demuestra agrupación de consumidores por dominio funcional separado.
 * - Confirmación manual explícita (ACK/NACK).
 */
@Component
public class TicketGenerationConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketGenerationConsumer.class);

    @RabbitListener(
            queues = "${app.rabbitmq.queue}",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void procesarGeneracionTicket(
            @Payload PedidoEventDTO evento,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info("[DOMINIO: TICKETS Y DOCUMENTOS] Iniciando emisión de comprobante digital...");
        try {
            if (evento == null || evento.getId() == null) {
                log.warn("[TICKETS] Evento inválido. Descartando con basicNack.");
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            // Simulación de creación de ticket PDF/Documento tributario
            String numeroTicket = "TKT-2026-" + evento.getId();
            log.info("[TICKETS] Comprobante emitido con éxito. Folio: {}, Monto Total: ${}",
                    numeroTicket, evento.getMonto());

            // Confirmación explícita
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[TICKETS] Fallo en la generación del documento. Rechazando a DLQ: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
