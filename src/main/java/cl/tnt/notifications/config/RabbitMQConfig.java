package cl.tnt.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ (misma declaración que el productor: es idempotente) y factory de listeners
 * con ACK manual. Los nombres vienen de application.yml (RabbitMQProperties).
 */
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    public RabbitMQConfig(RabbitMQProperties props) {
        this.props = props;
    }

    // ===================== EXCHANGES =====================

    /** Exchange de tipo TOPIC: enruta por patrón de routing key. */
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(props.exchanges().pedidos(), true, false);
    }

    /** Exchange de tipo DIRECT: enruta por routing key exacta. */
    @Bean
    public DirectExchange ticketsExchange() {
        return new DirectExchange(props.exchanges().tickets(), true, false);
    }

    /** Dead Letter Exchange (DIRECT) que recibe los mensajes rechazados de las 3 colas. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    // ===================== COLAS PRINCIPALES (cada una con su DLQ) =====================

    @Bean
    public Queue notificacionQueue() {
        return colaConDlq(props.queues().notificacion(), props.routingKeys().dlqNotificacion());
    }

    @Bean
    public Queue ticketQueue() {
        return colaConDlq(props.queues().ticket(), props.routingKeys().dlqTicket());
    }

    @Bean
    public Queue auditoriaQueue() {
        return colaConDlq(props.queues().auditoria(), props.routingKeys().dlqAuditoria());
    }

    // ===================== DEAD LETTER QUEUES =====================

    @Bean
    public Queue notificacionDlq() {
        return QueueBuilder.durable(props.dlqs().notificacion()).build();
    }

    @Bean
    public Queue ticketDlq() {
        return QueueBuilder.durable(props.dlqs().ticket()).build();
    }

    @Bean
    public Queue auditoriaDlq() {
        return QueueBuilder.durable(props.dlqs().auditoria()).build();
    }

    // ===================== BINDINGS =====================

    @Bean
    public Binding notificacionBinding(@Qualifier("notificacionQueue") Queue queue,
                                       @Qualifier("pedidosExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().pedidoCreado());
    }

    /** Patrón "pedidos.#": la auditoría recibe todos los eventos de pedidos. */
    @Bean
    public Binding auditoriaBinding(@Qualifier("auditoriaQueue") Queue queue,
                                    @Qualifier("pedidosExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().auditoria());
    }

    @Bean
    public Binding ticketBinding(@Qualifier("ticketQueue") Queue queue,
                                 @Qualifier("ticketsExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(props.routingKeys().ticket());
    }

    @Bean
    public Binding notificacionDlqBinding(@Qualifier("notificacionDlq") Queue queue,
                                          @Qualifier("deadLetterExchange") DirectExchange dlx) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().dlqNotificacion());
    }

    @Bean
    public Binding ticketDlqBinding(@Qualifier("ticketDlq") Queue queue,
                                    @Qualifier("deadLetterExchange") DirectExchange dlx) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().dlqTicket());
    }

    @Bean
    public Binding auditoriaDlqBinding(@Qualifier("auditoriaDlq") Queue queue,
                                       @Qualifier("deadLetterExchange") DirectExchange dlx) {
        return BindingBuilder.bind(queue).to(dlx).with(props.routingKeys().dlqAuditoria());
    }

    private Queue colaConDlq(String nombre, String dlqRoutingKey) {
        return QueueBuilder.durable(nombre)
                .withArgument("x-dead-letter-exchange", props.exchanges().dlx())
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false); // basicNack(requeue=false) -> DLQ
        return factory;
    }
}
