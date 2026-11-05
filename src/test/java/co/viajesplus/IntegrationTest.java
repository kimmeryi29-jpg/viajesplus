package co.viajesplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base de las pruebas de integración: PostgreSQL 16 real con Testcontainers (plan.md, ADR-002). Si la variable
 * TEST_DB_URL existe se usa esa base en lugar del contenedor. El reloj queda fijo en el 5 de noviembre de 2026.
 */
@SpringBootTest(properties = {"viajesplus.fixed-date=2026-11-05", "viajesplus.demo-password=Prueba2026"})
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    public static final String PASSWORD = "Prueba2026";

    private static PostgreSQLContainer<?> postgres;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("TEST_DB_URL");
        if (url != null && !url.isBlank()) {
            registry.add("spring.datasource.url", () -> url);
            registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USER", "viajesplus"));
            registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", "viajesplus"));
            return;
        }
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine");
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /** Pide un token al proveedor OIDC simulado, como lo haría un cliente de la API. */
    protected String token(String username) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("username", username, "password", PASSWORD));
        String response = mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(response);
        return "Bearer " + node.get("access_token").asText();
    }
}
