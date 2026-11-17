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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T013 · Pruebas de asignación y cruces, escritas antes de T014 y T015. AC-009.1 a AC-009.3 · REQ-VIA-009 ·
 * RN-05 y RN-06 · HU-20, HU-24 a HU-30.
 */
class AssignmentApiIT extends IntegrationTest {

    private static final String GUIA = "guia@viajesplus.co";
    private static final String GUIA2 = "guia2@viajesplus.co";
    private static final String CONDUCTOR = "conductor@viajesplus.co";
    private static final String CONDUCTOR2 = "conductor2@viajesplus.co";

    @Autowired
    JdbcTemplate jdbc;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
    }

    private long confirmada(Salida s) throws Exception {
        String body = json.writeValueAsString(Map.of("packageId", s.packageId(), "departureDate", s.date().toString(),
                "travelers", 2));
        String res = mvc.perform(post("/api/reservations").header("Authorization", token("cliente@viajesplus.co"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(res).get("id").asLong();
        mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token("agente@viajesplus.co")))
                .andExpect(status().isOk());
        return id;
    }

    private ResultActions asignar(long id, long hotel, String guia, String conductor) throws Exception {
        String body = json.writeValueAsString(Map.of("hotelId", hotel, "guideUsername", guia, "driverUsername", conductor));
        return mvc.perform(put("/api/reservations/{id}/assignment", id).header("Authorization", token("coordinador@viajesplus.co"))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    @DisplayName("HU-24 a HU-30: asigna hotel, guía y conductor y avisa al guía y al conductor")
    void asignaYAvisa() throws Exception {
        Salida s = data.salida(40, 10, true);
        long id = confirmada(s);
        String res = asignar(id, s.hotelId(), GUIA, CONDUCTOR).andExpect(status().isOk())
                .andExpect(jsonPath("$.guideUsername").value(GUIA))
                .andExpect(jsonPath("$.driverUsername").value(CONDUCTOR))
                .andReturn().getResponse().getContentAsString();
        String code = json.readTree(res).get("code").asText();
        for (String u : new String[]{GUIA, CONDUCTOR}) {
            JsonNode avisos = json.readTree(mvc.perform(get("/api/notifications").header("Authorization", token(u)))
                    .andReturn().getResponse().getContentAsString());
            assertThat(avisos.findValuesAsText("relatedCode")).contains(code);
        }
        mvc.perform(get("/api/reservations/{id}", id).header("Authorization", token(GUIA))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("AC-009.1: un hotel sin disponibilidad confirmada no se puede asignar")
    void hotelSinDisponibilidad() throws Exception {
        Salida s = data.salida(41, 10, true);
        long otroHotel = data.hotel(s.destination(), s.departureId(), false, "proveedor@viajesplus.co");
        long id = confirmada(s);
        asignar(id, otroHotel, GUIA, CONDUCTOR).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("HOTEL_SIN_DISPONIBILIDAD"));
    }

    @Test
    @DisplayName("AC-009.2: un guía no puede tener dos salidas distintas el mismo día")
    void cruceGuia() throws Exception {
        Salida a = data.salida(42, 10, true);
        Salida b = data.salida(42, 10, true);
        asignar(confirmada(a), a.hotelId(), GUIA, CONDUCTOR).andExpect(status().isOk());
        asignar(confirmada(b), b.hotelId(), GUIA, CONDUCTOR2).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CRUCE_GUIA"));
    }

    @Test
    @DisplayName("AC-009.3: un conductor no puede tener dos salidas distintas el mismo día")
    void cruceConductor() throws Exception {
        Salida a = data.salida(43, 10, true);
        Salida b = data.salida(43, 10, true);
        asignar(confirmada(a), a.hotelId(), GUIA, CONDUCTOR).andExpect(status().isOk());
        asignar(confirmada(b), b.hotelId(), GUIA2, CONDUCTOR).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CRUCE_CONDUCTOR"));
    }

    @Test
    @DisplayName("Dos grupos de la misma salida pueden compartir guía y conductor")
    void mismaSalida() throws Exception {
        Salida s = data.salida(44, 10, true);
        asignar(confirmada(s), s.hotelId(), GUIA, CONDUCTOR).andExpect(status().isOk());
        asignar(confirmada(s), s.hotelId(), GUIA, CONDUCTOR).andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU-24: una reserva PENDIENTE no se puede asignar")
    void pendienteNoSeAsigna() throws Exception {
        Salida s = data.salida(45, 10, true);
        String body = json.writeValueAsString(Map.of("packageId", s.packageId(), "departureDate", s.date().toString(),
                "travelers", 1));
        long id = json.readTree(mvc.perform(post("/api/reservations").header("Authorization", token("cliente@viajesplus.co"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getContentAsString())
                .get("id").asLong();
        asignar(id, s.hotelId(), GUIA, CONDUCTOR).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ESTADO_INVALIDO"));
    }

    @Test
    @DisplayName("HU-20: el proveedor confirma disponibilidad y desde ese momento el agente puede confirmar")
    void proveedorConfirmaDisponibilidad() throws Exception {
        Salida s = data.salida(46, 10, false);
        String body = json.writeValueAsString(Map.of("packageId", s.packageId(), "departureDate", s.date().toString(),
                "travelers", 2));
        long id = json.readTree(mvc.perform(post("/api/reservations").header("Authorization", token("cliente@viajesplus.co"))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getContentAsString())
                .get("id").asLong();
        mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token("agente@viajesplus.co")))
                .andExpect(jsonPath("$.code").value("SIN_DISPONIBILIDAD"));
        String disponibilidad = json.writeValueAsString(Map.of("hotelId", s.hotelId(), "departureId", s.departureId(),
                "rooms", 8, "ratePerNight", 300000, "confirmed", true));
        mvc.perform(put("/api/providers/availability").header("Authorization", token("proveedor@viajesplus.co"))
                        .contentType(MediaType.APPLICATION_JSON).content(disponibilidad))
                .andExpect(status().isOk()).andExpect(jsonPath("$.confirmed").value(true));
        mvc.perform(post("/api/reservations/{id}/confirm", id).header("Authorization", token("agente@viajesplus.co")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU-25: un proveedor no puede cambiar la disponibilidad de un hotel de otro proveedor (403)")
    void hotelAjeno() throws Exception {
        Salida s = data.salida(47, 10, false);
        long ajeno = data.hotel(s.destination(), s.departureId(), false, "otro.proveedor@viajesplus.co");
        String body = json.writeValueAsString(Map.of("hotelId", ajeno, "departureId", s.departureId(),
                "rooms", 5, "ratePerNight", 200000, "confirmed", true));
        mvc.perform(put("/api/providers/availability").header("Authorization", token("proveedor@viajesplus.co"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Solo el coordinador asigna: el agente recibe 403")
    void soloCoordinador() throws Exception {
        Salida s = data.salida(48, 10, true);
        long id = confirmada(s);
        String body = json.writeValueAsString(Map.of("hotelId", s.hotelId(), "guideUsername", GUIA,
                "driverUsername", CONDUCTOR));
        mvc.perform(put("/api/reservations/{id}/assignment", id).header("Authorization", token("agente@viajesplus.co"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }
}
