package cl.tnt.notifications.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStatusController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "servicio", "ms-notificaciones",
                "estado", "ONLINE",
                "dominio", "Consumidores Asíncronos RabbitMQ (Notificaciones, Tickets, DLQ)",
                "ackMode", "MANUAL",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
