package co.viajesplus.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

/**
 * T004 · Seguridad OIDC y 10 roles. Trazabilidad: REQ-VIA-012, NFR-VIA-002, Constitución 4 (mínimo privilegio).
 * La API valida tokens JWT (OIDC). Mientras no exista el tenant real (IMP-01) los emite SimulatedOidcProvider;
 * con viajesplus.oidc.issuer-uri configurado se validan los del proveedor real sin cambiar el resto del código.
 * Las reglas de los sprints 2 y 3 ya están declaradas: así un rol sin permiso recibe 403 desde el primer día.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtDecoder jwtDecoder(SimulatedOidcProvider simulated,
                          @Value("${viajesplus.oidc.issuer-uri:}") String issuerUri) {
        if (!issuerUri.isBlank()) {
            return JwtDecoders.fromIssuerLocation(issuerUri);
        }
        return NimbusJwtDecoder.withPublicKey(simulated.publicKey()).build();
    }

    private static JwtAuthenticationConverter rolesConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            Collection<GrantedAuthority> authorities = roles == null ? List.of()
                    : roles.stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r)).toList();
            return authorities;
        });
        return converter;
    }

    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/jwks").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/packages", "/api/packages/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/packages/{id}/quote").hasAnyRole("CLIENTE", "ASESOR", "AGENTE")
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasRole("CLIENTE")
                        // Sprint 2: decisión del agente y asignación del coordinador
                        .requestMatchers(HttpMethod.POST, "/api/reservations/{id}/confirm", "/api/reservations/{id}/reject").hasRole("AGENTE")
                        .requestMatchers("/api/reservations/{id}/assignment").hasRole("COORDINADOR")
                        .requestMatchers("/api/providers/**").hasRole("PROVEEDOR")
                        // Sprint 3: cancelación del cliente
                        .requestMatchers(HttpMethod.POST, "/api/reservations/{id}/cancel").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/reservations", "/api/reservations/{id}").authenticated()
                        .requestMatchers("/api/audit/**").hasAnyRole("GERENTE", "ADMIN")
                        // Objetivo extendido y backlog futuro
                        .requestMatchers("/api/tours/**").hasAnyRole("GUIA", "CONDUCTOR", "COORDINADOR")
                        .requestMatchers("/api/payments/**").hasRole("CONTADOR")
                        .requestMatchers("/api/reports/**").hasRole("GERENTE")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesConverter()))
                        .authenticationEntryPoint(JsonSecurityHandlers.unauthorized())
                        .accessDeniedHandler(JsonSecurityHandlers.forbidden()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(JsonSecurityHandlers.unauthorized())
                        .accessDeniedHandler(JsonSecurityHandlers.forbidden()));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/paquetes/**", "/login", "/error", "/acceso-denegado",
                                "/css/**", "/img/**", "/favicon.ico",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/reservas/**").hasRole("CLIENTE")
                        .requestMatchers("/asesor/**").hasRole("ASESOR")
                        .requestMatchers("/auditoria/**").hasAnyRole("GERENTE", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/panel", true).permitAll())
                .logout(l -> l.logoutUrl("/salir").logoutSuccessUrl("/?salida"))
                .exceptionHandling(e -> e.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
