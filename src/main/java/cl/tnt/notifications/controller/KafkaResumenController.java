package cl.tnt.notifications.controller;

import cl.tnt.notifications.dto.PedidoEventDTO;
import cl.tnt.notifications.service.AnaliticaService;
import cl.tnt.notifications.service.EstadoPedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Consulta de lo procesado desde Kafka (útil para la demostración). */
@RestController
@RequestMapping("/api/notifications/kafka")
public class KafkaResumenController {

    private final AnaliticaService analiticaService;
    private final EstadoPedidoService estadoPedidoService;

    public KafkaResumenController(AnaliticaService analiticaService, EstadoPedidoService estadoPedidoService) {
        this.analiticaService = analiticaService;
        this.estadoPedidoService = estadoPedidoService;
    }

    @GetMapping("/analitica")
    public ResponseEntity<Map<String, Object>> analitica() {
        return ResponseEntity.ok(analiticaService.resumen());
    }

    @GetMapping("/estados")
    public ResponseEntity<Map<Long, PedidoEventDTO>> estados() {
        return ResponseEntity.ok(estadoPedidoService.estadosActuales());
    }
}
