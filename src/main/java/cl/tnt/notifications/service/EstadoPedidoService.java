package cl.tnt.notifications.service;

import cl.tnt.notifications.dto.PedidoEventDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Mantiene el último estado conocido de cada pedido (vista materializada del tópico compactado). */
@Service
public class EstadoPedidoService {

    private final Map<Long, PedidoEventDTO> estados = new ConcurrentHashMap<>();

    public void actualizar(PedidoEventDTO evento) {
        PedidoValidador.validar(evento);
        estados.put(evento.getId(), evento);
    }

    public Map<Long, PedidoEventDTO> estadosActuales() {
        return Map.copyOf(estados);
    }
}
