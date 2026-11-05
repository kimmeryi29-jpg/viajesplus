package co.viajesplus.domain;

/** Estados de una reserva. En el Sprint 1 solo se crea PENDIENTE; el resto llega en los sprints 2 y 3. */
public enum ReservationStatus {
    PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA
}
