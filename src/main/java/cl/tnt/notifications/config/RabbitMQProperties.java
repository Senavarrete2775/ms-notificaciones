package cl.tnt.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nombres de exchanges, colas, DLQ y routing keys, leídos de application.yml (prefijo app.rabbitmq).
 * Único lugar donde viven los nombres: el resto del código los recibe de aquí.
 */
@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(Exchanges exchanges, Queues queues, Queues dlqs, RoutingKeys routingKeys) {

    public record Exchanges(String pedidos, String tickets, String dlx) {}

    public record Queues(String notificacion, String ticket, String auditoria) {}

    public record RoutingKeys(String pedidoCreado, String ticket, String auditoria,
                              String dlqNotificacion, String dlqTicket, String dlqAuditoria) {}
}
