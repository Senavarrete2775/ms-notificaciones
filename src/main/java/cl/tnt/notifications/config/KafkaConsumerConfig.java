package cl.tnt.notifications.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Manejo de errores de los consumidores Kafka:
 *  - errores transitorios: hasta 3 reintentos, 1 segundo entre cada uno;
 *  - datos inválidos (IllegalArgumentException) y mensajes que no se pueden deserializar: sin reintentos;
 *  - al agotarse los reintentos el registro se descarta con un log detallado y el consumo continúa,
 *    de modo que un mensaje defectuoso no bloquea la partición.
 * Spring Boot aplica este bean a todos los @KafkaListener.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (registro, excepcion) -> log.error(
                        "[KAFKA][DESCARTADO] topic={} partición={} offset={} clave={} causa={}",
                        registro.topic(), registro.partition(), registro.offset(), registro.key(),
                        excepcion.getCause() != null ? excepcion.getCause().getMessage() : excepcion.getMessage()),
                new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }
}
