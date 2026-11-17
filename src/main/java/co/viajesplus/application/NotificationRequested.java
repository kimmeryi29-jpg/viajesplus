package co.viajesplus.application;

/** Evento interno (ADR-004): pide enviar un aviso cuando la transacción del cambio se confirma. */
public record NotificationRequested(String recipient, String subject, String body, String relatedCode) {
}
