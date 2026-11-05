package co.viajesplus.api;

import co.viajesplus.IntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de punta a punta de la API del Sprint 1 contra PostgreSQL real.
 * T004 (seguridad), T005 (auditoría) y T008 (solicitud de reserva).
 */
class ReservationApiIT extends IntegrationTest {

    private static final String CLIENTE = "cliente@viajesplus.co";
    private static final String CLIENTE2 = "cliente2@viajesplus.co";

    private long cartagenaId() throws Exception {
        String body = mvc.perform(get("/api/packages")).andReturn().getResponse().getContentAsString();
        for (JsonNode p : json.readTree(body)) {
            if ("CTG-01".equals(p.get("code").asText())) {
                return p.get("id").asLong();
            }
        }
        throw new IllegalStateException("No está el paquete CTG-01");
    }

    private JsonNode solicitar(String token, Map<String, Object> body, int expected) throws Exception {
        String res = mvc.perform(post("/api/reservations").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    @Test
    @DisplayName("AC-001.1: el cliente cotiza Cartagena para 3 viajeros y el total es $3.600.000")
    void cotizacionCliente() throws Exception {
        mvc.perform(get("/api/packages/{id}/quote", cartagenaId()).param("travelers", "3")
                        .header("Authorization", token(CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerPerson").value(1200000.00))
                .andExpect(jsonPath("$.total").value(3600000.00));
    }

    @Test
    @DisplayName("HU-12: el asesor obtiene la misma cotización que el cliente")
    void cotizacionAsesor() throws Exception {
        mvc.perform(get("/api/packages/{id}/quote", cartagenaId()).param("travelers", "3")
                        .header("Authorization", token("asesor@viajesplus.co")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3600000.00));
    }

    @Test
    @DisplayName("AC-003.1: una solicitud válida queda PENDIENTE con su total, y AC-011.1: queda auditada")
    void solicitudValidaYAuditada() throws Exception {
        JsonNode r = solicitar(token(CLIENTE),
                Map.of("packageId", cartagenaId(), "departureDate", "2027-01-15", "travelers", 3), 201);
        assertThat(r.get("status").asText()).isEqualTo("PENDIENTE");
        assertThat(r.get("total").decimalValue()).isEqualByComparingTo("3600000");
        String code = r.get("code").asText();
        assertThat(code).startsWith("RES-");

        mvc.perform(get("/api/audit").param("reservation", code).header("Authorization", token("gerente@viajesplus.co")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SOLICITAR"))
                .andExpect(jsonPath("$[0].toStatus").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].username").value(CLIENTE));
    }

    @Test
    @DisplayName("AC-003.2 (error): sin fecha de salida responde 422 e indica el campo")
    void sinFecha() throws Exception {
        JsonNode e = solicitar(token(CLIENTE), Map.of("packageId", cartagenaId(), "travelers", 2), 422);
        assertThat(e.get("code").asText()).isEqualTo("DATOS_INVALIDOS");
        assertThat(e.get("details").has("departureDate")).isTrue();
    }

    @Test
    @DisplayName("AC-003.2 (error): con 0 viajeros responde 422 e indica el campo")
    void ceroViajeros() throws Exception {
        JsonNode e = solicitar(token(CLIENTE),
                Map.of("packageId", cartagenaId(), "departureDate", "2027-01-15", "travelers", 0), 422);
        assertThat(e.get("details").has("travelers")).isTrue();
    }

    @Test
    @DisplayName("Una fecha sin salida programada responde 422")
    void fechaSinSalida() throws Exception {
        JsonNode e = solicitar(token(CLIENTE),
                Map.of("packageId", cartagenaId(), "departureDate", "2027-01-16", "travelers", 2), 422);
        assertThat(e.get("details").has("departureDate")).isTrue();
    }

    @Test
    @DisplayName("AC-012.1: un cliente no puede ver la reserva de otro cliente (403)")
    void reservaAjena() throws Exception {
        JsonNode r = solicitar(token(CLIENTE),
                Map.of("packageId", cartagenaId(), "departureDate", "2027-03-19", "travelers", 1), 201);
        long id = r.get("id").asLong();
        mvc.perform(get("/api/reservations/{id}", id).header("Authorization", token(CLIENTE2)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservations/{id}", id).header("Authorization", token(CLIENTE)))
                .andExpect(status().isOk());
        String lista = mvc.perform(get("/api/reservations").header("Authorization", token(CLIENTE2)))
                .andReturn().getResponse().getContentAsString();
        for (JsonNode n : json.readTree(lista)) {
            assertThat(n.get("clientUsername").asText()).isEqualTo(CLIENTE2);
        }
    }

    @Test
    @DisplayName("AC-012.2: un cliente que intenta confirmar una reserva recibe 403")
    void clienteNoConfirma() throws Exception {
        mvc.perform(post("/api/reservations/{id}/confirm", 1).header("Authorization", token(CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
    }

    @Test
    @DisplayName("El cliente no puede consultar la auditoría (403)")
    void clienteSinAuditoria() throws Exception {
        mvc.perform(get("/api/audit").header("Authorization", token(CLIENTE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AC-012.3: credenciales inválidas responden 401 y sin token la API responde 401")
    void credencialesInvalidas() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente@viajesplus.co\",\"password\":\"otra\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CREDENCIALES_INVALIDAS"));
        mvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU-06: el token trae el rol del usuario")
    void tokenConRol() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"agente@viajesplus.co\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENTE"))
                .andExpect(jsonPath("$", hasKey("access_token")));
    }
}
