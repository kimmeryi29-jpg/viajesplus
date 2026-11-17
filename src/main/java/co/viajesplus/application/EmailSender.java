package co.viajesplus.application;

import co.viajesplus.infrastructure.persistence.Notification;

/** Puerto de salida del correo (ADR-004). Devuelve true si el envío fue aceptado. */
public interface EmailSender {

    boolean send(Notification notification);
}
