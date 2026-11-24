package cl.tnt.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el panel Angular (otro origen en desarrollo, p. ej. localhost:4200) consulte
 * los endpoints de solo lectura. En la nube el acceso va por Caddy bajo el mismo dominio.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/notifications/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "OPTIONS");
    }
}
