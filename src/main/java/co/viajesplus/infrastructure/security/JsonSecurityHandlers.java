package co.viajesplus.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Respuestas 401 y 403 de la API con el mismo formato { "code", "message" } del resto de errores. */
final class JsonSecurityHandlers {

    private JsonSecurityHandlers() {
    }

    static AuthenticationEntryPoint unauthorized() {
        return (request, response, ex) -> write(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO",
                "Inicia sesión para usar este recurso");
    }

    static AccessDeniedHandler forbidden() {
        return (request, response, ex) -> write(response, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO",
                "Tu rol no tiene permiso para esta operación");
    }

    private static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"details\":{}}");
    }
}
