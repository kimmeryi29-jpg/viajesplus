package co.viajesplus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pantallas sencillas del Sprint 1. */
class WebIT extends IntegrationTest {

    @Test
    @DisplayName("El catálogo es público y muestra los paquetes")
    void catalogo() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Cartagena histórica")));
    }

    @Test
    @DisplayName("Mis reservas exige iniciar sesión")
    void reservasPideLogin() throws Exception {
        mvc.perform(get("/reservas")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("El cliente inicia sesión en la web y llega a su panel")
    void loginWeb() throws Exception {
        mvc.perform(formLogin("/login").user("cliente@viajesplus.co").password(PASSWORD))
                .andExpect(redirectedUrl("/panel"));
        mvc.perform(formLogin("/login").user("cliente@viajesplus.co").password("mala"))
                .andExpect(redirectedUrl("/login?error"));
    }
}
