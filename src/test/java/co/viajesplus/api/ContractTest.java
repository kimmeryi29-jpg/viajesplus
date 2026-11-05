package co.viajesplus.api;

import co.viajesplus.IntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** DoD 9: el contrato api/openapi.yaml y la API implementada exponen las mismas rutas y métodos. */
class ContractTest extends IntegrationTest {

    @Test
    @DisplayName("Cada operación de api/openapi.yaml existe en la API y viceversa")
    @SuppressWarnings("unchecked")
    void contratoIgualALaApi() throws Exception {
        Set<String> contrato = new TreeSet<>();
        try (InputStream in = Files.newInputStream(Path.of("api", "openapi.yaml"))) {
            Map<String, Object> doc = new Yaml().load(in);
            Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) doc.get("paths");
            paths.forEach((ruta, ops) -> ops.keySet().forEach(m -> contrato.add(m.toUpperCase() + " " + ruta)));
        }

        String body = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        JsonNode paths = json.readTree(body).get("paths");
        Set<String> implementado = new TreeSet<>();
        paths.fields().forEachRemaining(e -> e.getValue().fieldNames()
                .forEachRemaining(m -> implementado.add(m.toUpperCase() + " " + e.getKey())));

        assertThat(implementado).isEqualTo(contrato);
    }
}
