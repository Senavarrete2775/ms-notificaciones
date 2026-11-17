package cl.tnt.notifications.service;

import cl.tnt.notifications.dto.PedidoEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Lógica de negocio de notificaciones (independiente de Kafka/RabbitMQ). */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void notificar(PedidoEventDTO evento) {
        PedidoValidador.validar(evento);
        log.info("[NOTIFICACIÓN] Enviando aviso al cliente '{}' por su pedido #{} de '{}' (${}).",
                evento.getCliente(), evento.getId(), evento.getProducto(), evento.getMonto());
    }
}
