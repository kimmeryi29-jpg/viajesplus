package co.viajesplus.api;

import co.viajesplus.IntegrationTest;
import co.viajesplus.TestData;
import co.viajesplus.TestData.Salida;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T009 · Las 14 pruebas de confirmación, escritas antes de T010 y T011 (estaban en rojo el lunes 9/11).
 * AC-004.1 a 004.3 · AC-005.1 · AC-006.1, 006.2 · AC-007.1, 007.2 · AC-008.1, 008.2 · HU-16 · NFR-VIA-003.
 */
class DecisionApiIT extends IntegrationTest {

    private static final String CLIENTE = "cliente@viajesplus.co";
    private static final String CLIENTE2 = "cliente2@viajesplus.co";
    private static final String AGENTE = "agente@viajesplus.co";

    @Autowired
    JdbcTemplate jdbc;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
    }

    private ResultActions solicitud(String token, Salida s, int viajeros) throws Exception {
        String body = json.writeValueAsString(Map.of("packageId", s.packageId(), "departureDate", s.date().toString(),
                "travelers", viajeros));
        return mvc.perform(post("/api/reservations").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long solicitar(Salida s, int viajeros) throws Exception {
        String res = solicitud(token(CLIENTE), s, viajeros).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("id").asLong();
    }

    private ResultActions confirmar(long id) throws Exception {
        return mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token(AGENTE)));
    }

    private ResultActions rechazar(long id, String motivo) throws Exception {
        String body = motivo == null ? "{}" : json.writeValueAsString(Map.of("reason", motivo));
        return mvc.perform(post("/api/reservations/{id}/reject", id).header("Authorization", token(AGENTE))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    @DisplayName("AC-007.1: una reserva que cumple las reglas pasa a CONFIRMADA, se audita y el cliente recibe el aviso")
    void confirmaYAvisa() throws Exception {
        Salida s = data.salida(20, 10, true);
        long id = solicitar(s, 2);
        String code = confirmar(id).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.decidedBy").value(AGENTE))
                .andReturn().getResponse().getContentAsString();
        String codigo = json.readTree(code).get("code").asText();
        mvc.perform(get("/api/audit").param("reservation", codigo).header("Authorization", token("gerente@viajesplus.co")))
                .andExpect(jsonPath("$[1].action").value("CONFIRMAR"))
                .andExpect(jsonPath("$[1].toStatus").value("CONFIRMADA"));
        JsonNode avisos = json.readTree(mvc.perform(get("/api/notifications").header("Authorization", token(CLIENTE)))
                .andReturn().getResponse().getContentAsString());
        assertThat(avisos.findValuesAsText("relatedCode")).contains(codigo);
    }

    @Test
    @DisplayName("NFR-VIA-003: el aviso al cliente queda ENVIADO en menos de 60 segundos tras la decisión")
    void avisoEnMenosDe60s() throws Exception {
        Salida s = data.salida(21, 10, true);
        long id = solicitar(s, 1);
        JsonNode r = json.readTree(confirmar(id).andReturn().getResponse().getContentAsString());
        Instant decidida = Instant.parse(r.get("decidedAt").asText());
        JsonNode avisos = json.readTree(mvc.perform(get("/api/notifications").header("Authorization", token(CLIENTE)))
                .andReturn().getResponse().getContentAsString());
        JsonNode aviso = null;
        for (JsonNode a : avisos) {
            if (r.get("code").asText().equals(a.get("relatedCode").asText())) {
                aviso = a;
            }
        }
        assertThat(aviso).isNotNull();
        assertThat(aviso.get("status").asText()).isEqualTo("ENVIADO");
        Instant enviado = Instant.parse(aviso.get("sentAt").asText());
        assertThat(Duration.between(decidida, enviado)).isLessThan(Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("AC-004.1: con 2 plazas libres, una solicitud de 3 viajeros responde CUPO_INSUFICIENTE")
    void sinCupo() throws Exception {
        Salida s = data.salida(22, 5, true);
        long id = solicitar(s, 3);
        confirmar(id).andExpect(status().isOk());
        solicitud(token(CLIENTE2), s, 3).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CUPO_INSUFICIENTE"));
    }

    @Test
    @DisplayName("AC-004.2 (borde): con 3 plazas libres, una solicitud de 3 viajeros se acepta y quedan 0")
    void cupoExacto() throws Exception {
        Salida s = data.salida(23, 3, true);
        long id = solicitar(s, 3);
        assertThat(data.plazasOcupadas(s.departureId())).isEqualTo(3);
        confirmar(id).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("AC-004.3 (spec v1.0.1, IMP-02): las reservas PENDIENTE también ocupan cupo")
    void pendientesOcupanCupo() throws Exception {
        Salida s = data.salida(24, 4, true);
        solicitar(s, 3);
        solicitud(token(CLIENTE2), s, 2).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CUPO_INSUFICIENTE"));
        assertThat(data.plazasOcupadas(s.departureId())).isEqualTo(3);
    }

    @Test
    @DisplayName("AC-005.1: dos solicitudes simultáneas por la última plaza: solo una se acepta")
    void ultimaPlazaSimultanea() throws Exception {
        Salida s = data.salida(25, 1, true);
        String t1 = token(CLIENTE);
        String t2 = token(CLIENTE2);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (String t : List.of(t1, t2)) {
                Callable<Integer> c = () -> {
                    salida.await();
                    return solicitud(t, s, 1).andReturn().getResponse().getStatus();
                };
                resultados.add(pool.submit(c));
            }
            salida.countDown();
            List<Integer> codigos = new ArrayList<>();
            for (Future<Integer> f : resultados) {
                codigos.add(f.get());
            }
            assertThat(codigos).containsExactlyInAnyOrder(201, 422);
        } finally {
            pool.shutdown();
        }
        assertThat(data.plazasOcupadas(s.departureId())).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-006.1: a 4 días de la salida responde ANTELACION_INSUFICIENTE y la reserva sigue PENDIENTE")
    void cuatroDias() throws Exception {
        Salida s = data.salida(4, 10, true);
        long id = solicitar(s, 2);
        confirmar(id).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ANTELACION_INSUFICIENTE"));
        mvc.perform(get("/api/reservations/{id}", id).header("Authorization", token(CLIENTE)))
                .andExpect(jsonPath("$.status").value("PENDIENTE"));
    }

    @Test
    @DisplayName("AC-006.2 (borde): a 5 días de la salida sí se confirma")
    void cincoDias() throws Exception {
        Salida s = data.salida(5, 10, true);
        long id = solicitar(s, 2);
        confirmar(id).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("AC-007.2: sin disponibilidad confirmada del proveedor responde SIN_DISPONIBILIDAD")
    void sinDisponibilidad() throws Exception {
        Salida s = data.salida(26, 10, false);
        long id = solicitar(s, 2);
        confirmar(id).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SIN_DISPONIBILIDAD"));
    }

    @Test
    @DisplayName("AC-008.1: rechazar sin motivo responde MOTIVO_OBLIGATORIO")
    void rechazoSinMotivo() throws Exception {
        Salida s = data.salida(27, 10, true);
        long id = solicitar(s, 2);
        rechazar(id, null).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("MOTIVO_OBLIGATORIO"));
        rechazar(id, "   ").andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("AC-008.2: rechazar con motivo deja RECHAZADA, libera el cupo y avisa al cliente")
    void rechazoConMotivo() throws Exception {
        Salida s = data.salida(28, 3, true);
        long id = solicitar(s, 3);
        rechazar(id, "El vuelo de conexión fue cancelado").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADA"))
                .andExpect(jsonPath("$.rejectionReason").value("El vuelo de conexión fue cancelado"));
        assertThat(data.plazasOcupadas(s.departureId())).isZero();
        solicitud(token(CLIENTE2), s, 3).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Solo se decide una reserva PENDIENTE: confirmar dos veces responde ESTADO_INVALIDO")
    void yaConfirmada() throws Exception {
        Salida s = data.salida(29, 10, true);
        long id = solicitar(s, 1);
        confirmar(id).andExpect(status().isOk());
        confirmar(id).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("ESTADO_INVALIDO"));
    }

    @Test
    @DisplayName("AC-012.2: el asesor y el coordinador no pueden confirmar (403)")
    void otrosRolesNoConfirman() throws Exception {
        Salida s = data.salida(30, 10, true);
        long id = solicitar(s, 1);
        mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token("asesor@viajesplus.co")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token("coordinador@viajesplus.co")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU-16: la bandeja del agente trae solo PENDIENTE, de la salida más próxima a la más lejana")
    void bandejaOrdenada() throws Exception {
        Salida lejana = data.salida(60, 10, true);
        Salida cercana = data.salida(31, 10, true);
        solicitar(lejana, 1);
        solicitar(cercana, 1);
        long confirmada = solicitar(data.salida(32, 10, true), 1);
        confirmar(confirmada).andExpect(status().isOk());
        JsonNode bandeja = json.readTree(mvc.perform(get("/api/reservations").param("status", "PENDIENTE")
                .header("Authorization", token(AGENTE))).andReturn().getResponse().getContentAsString());
        LocalDate anterior = LocalDate.MIN;
        for (JsonNode r : bandeja) {
            assertThat(r.get("status").asText()).isEqualTo("PENDIENTE");
            LocalDate fecha = LocalDate.parse(r.get("departureDate").asText());
            assertThat(fecha).isAfterOrEqualTo(anterior);
            anterior = fecha;
        }
    }
}
