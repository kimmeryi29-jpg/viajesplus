package co.viajesplus.application;

/** El recurso existe pero pertenece a otra persona (REQ-VIA-012); la API responde 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
