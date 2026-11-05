package co.viajesplus.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    /**
     * Hora de Colombia para todas las fechas de negocio. Con viajesplus.fixed-date (aaaa-mm-dd) el reloj queda fijo,
     * para que las pruebas no dependan del día en que se ejecutan.
     */
    @Bean
    Clock clock(@Value("${viajesplus.fixed-date:}") String fixedDate) {
        if (fixedDate.isBlank()) {
            return Clock.system(BOGOTA);
        }
        return Clock.fixed(LocalDate.parse(fixedDate).atTime(9, 0).atZone(BOGOTA).toInstant(), BOGOTA);
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("ViajesPlus API").version("v1 · Sprint 1")
                        .description("Reservas de paquetes turísticos. Pide un token en POST /api/auth/token "
                                + "(proveedor OIDC simulado, IMP-01) y pégalo en Authorize."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
