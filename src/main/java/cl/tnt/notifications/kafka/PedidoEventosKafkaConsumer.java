package cl.tnt.notifications.kafka;

import cl.tnt.notifications.dto.PedidoEventDTO;
import cl.tnt.notifications.service.AnaliticaService;
import cl.tnt.notifications.service.EstadoPedidoService;
import cl.tnt.notifications.service.NotificacionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidores Kafka. Solo reciben el mensaje y delegan en los servicios de dominio.
 *
 * Consumer groups:
 *  - notificaciones-group y analitica-group leen el MISMO tópico (pedidos.eventos): cada grupo recibe
 *    todos los eventos. Dentro de un grupo, las 3 particiones se reparten entre sus consumidores.
 *  - estado-group lee pedidos.estado.
 * Si el servicio lanza una excepción, el DefaultErrorHandler (KafkaConsumerConfig) la registra y controla reintentos.
 */
@Component
public class PedidoEventosKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventosKafkaConsumer.class);

    private final NotificacionService notificacionService;
    private final AnaliticaService analiticaService;
    private final EstadoPedidoService estadoPedidoService;

    public PedidoEventosKafkaConsumer(NotificacionService notificacionService,
                                      AnaliticaService analiticaService,
                                      EstadoPedidoService estadoPedidoService) {
        this.notificacionService = notificacionService;
        this.analiticaService = analiticaService;
        this.estadoPedidoService = estadoPedidoService;
    }

    @KafkaListener(topics = "${app.kafka.topics.eventos}", groupId = "${app.kafka.groups.notificaciones}")
    public void notificaciones(ConsumerRecord<String, PedidoEventDTO> registro) {
        logRecepcion("notificaciones-group", registro);
        notificacionService.notificar(registro.value());
    }

    @KafkaListener(topics = "${app.kafka.topics.eventos}", groupId = "${app.kafka.groups.analitica}")
    public void analitica(ConsumerRecord<String, PedidoEventDTO> registro) {
        logRecepcion("analitica-group", registro);
        analiticaService.registrar(registro.value());
    }

    @KafkaListener(topics = "${app.kafka.topics.estado}", groupId = "${app.kafka.groups.estado}")
    public void estado(ConsumerRecord<String, PedidoEventDTO> registro) {
        logRecepcion("estado-group", registro);
        estadoPedidoService.actualizar(registro.value());
    }

    private void logRecepcion(String grupo, ConsumerRecord<String, PedidoEventDTO> r) {
        log.info("[KAFKA][{}] topic={} partición={} offset={} clave={}",
                grupo, r.topic(), r.partition(), r.offset(), r.key());
    }
}
