package co.viajesplus.api;

import java.util.Map;

/** Formato único de error del contrato: { "code": ..., "message": ..., "details": {campo: mensaje} }. */
public record ApiError(String code, String message, Map<String, String> details) {
}
