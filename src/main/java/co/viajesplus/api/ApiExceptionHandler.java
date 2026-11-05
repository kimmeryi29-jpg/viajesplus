package co.viajesplus.api;

import co.viajesplus.application.ForbiddenException;
import co.viajesplus.application.NotFoundException;
import co.viajesplus.domain.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Errores de negocio y de datos responden 422; recurso ajeno 403; inexistente 404 (contrato de openapi.yaml). */
@RestControllerAdvice(basePackages = "co.viajesplus.api")
public class ApiExceptionHandler {

    private static final HttpStatus UNPROCESSABLE = HttpStatus.UNPROCESSABLE_ENTITY;

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(BusinessException e) {
        return ResponseEntity.status(UNPROCESSABLE).body(new ApiError(e.getCode(), e.getMessage(), e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        Map<String, String> details = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> details.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.status(UNPROCESSABLE)
                .body(new ApiError("DATOS_INVALIDOS", "Revisa los datos de la solicitud", details));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> missing(MissingServletRequestParameterException e) {
        return ResponseEntity.status(UNPROCESSABLE).body(new ApiError("DATOS_INVALIDOS",
                "Falta el parámetro " + e.getParameterName(), Map.of(e.getParameterName(), "Es obligatorio")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> mismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(UNPROCESSABLE).body(new ApiError("DATOS_INVALIDOS",
                "Valor no válido para " + e.getName(), Map.of(e.getName(), "Valor no válido")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(UNPROCESSABLE).body(new ApiError("DATOS_INVALIDOS",
                "El cuerpo de la solicitud no es válido (revisa el formato de la fecha: aaaa-mm-dd)", Map.of()));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NO_ENCONTRADO", e.getMessage(), Map.of()));
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiError> forbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError("ACCESO_DENEGADO", e.getMessage(), Map.of()));
    }
}
