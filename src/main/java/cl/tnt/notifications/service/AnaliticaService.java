package cl.tnt.notifications.service;

import cl.tnt.notifications.dto.PedidoEventDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/** Métricas en memoria calculadas a partir del flujo de eventos (analítica en streaming). */
@Service
public class AnaliticaService {

    private final AtomicLong totalPedidos = new AtomicLong();
    private final DoubleAdder montoTotal = new DoubleAdder();
    private final Map<String, AtomicLong> pedidosPorCliente = new ConcurrentHashMap<>();

    public void registrar(PedidoEventDTO evento) {
        PedidoValidador.validar(evento);
        totalPedidos.incrementAndGet();
        montoTotal.add(evento.getMonto());
        pedidosPorCliente.computeIfAbsent(String.valueOf(evento.getCliente()), c -> new AtomicLong())
                .incrementAndGet();
    }

    public Map<String, Object> resumen() {
        long total = totalPedidos.get();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalPedidos", total);
        r.put("montoTotal", montoTotal.sum());
        r.put("ticketPromedio", total == 0 ? 0.0 : montoTotal.sum() / total);
        Map<String, Long> porCliente = new LinkedHashMap<>();
        pedidosPorCliente.forEach((k, v) -> porCliente.put(k, v.get()));
        r.put("pedidosPorCliente", porCliente);
        return r;
    }
}
