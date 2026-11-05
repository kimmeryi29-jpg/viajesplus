package co.viajesplus.domain;

import java.util.Map;

/**
 * Error de negocio con un código estable del contrato (por ejemplo DATOS_INVALIDOS o CUPO_INSUFICIENTE).
 * La capa API lo responde como 422 con { "code": ..., "details": ... }.
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final Map<String, String> details;

    public BusinessException(String code, String message, Map<String, String> details) {
        super(message);
        this.code = code;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public static BusinessException invalidField(String field, String message) {
        return new BusinessException("DATOS_INVALIDOS", message, Map.of(field, message));
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
