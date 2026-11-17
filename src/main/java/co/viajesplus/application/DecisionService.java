package co.viajesplus.application;

import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.ReservationStatus;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.HotelAvailabilityRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import co.viajesplus.infrastructure.persistence.ReservationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * T010 · Caso de uso DecideReservation. HU2: el agente de reservas confirma o rechaza una reserva PENDIENTE.
 * Trazabilidad: REQ-VIA-004 a REQ-VIA-008, REQ-VIA-011 · RN-02, RN-03, RN-04, RN-05 · AC-006.x, AC-007.x, AC-008.x.
 * El cupo ya quedó ocupado al solicitar (RN-02, spec v1.0.1); por eso confirmar no vuelve a ocuparlo y rechazar lo libera.
 */
@Service
public class DecisionService {

    /** RN-04: antelación mínima en días calendario. */
    public static final int MIN_DAYS_BEFORE_DEPARTURE = 5;

    private final ReservationRepository reservations;
    private final DepartureRepository departures;
    private final HotelAvailabilityRepository availability;
    private final AuditService audit;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public DecisionService(ReservationRepository reservations, DepartureRepository departures,
                           HotelAvailabilityRepository availability, AuditService audit,
                           ApplicationEventPublisher events, Clock clock) {
        this.reservations = reservations;
        this.departures = departures;
        this.availability = availability;
        this.audit = audit;
        this.events = events;
        this.clock = clock;
    }

    private Reservation pending(Long id) {
        Reservation r = reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la reserva " + id));
        if (r.getStatus() != ReservationStatus.PENDIENTE) {
            throw new BusinessException("ESTADO_INVALIDO", "Solo se decide una reserva PENDIENTE; esta está " + r.getStatus(),
                    Map.of("status", r.getStatus().name()));
        }
        return r;
    }

    /** Días calendario entre hoy y la salida (RN-04). */
    public long daysBefore(Departure d) {
        return ChronoUnit.DAYS.between(LocalDate.now(clock), d.getDepartureDate());
    }

    /** REQ-VIA-006 y REQ-VIA-007. */
    @Transactional
    public Reservation confirm(CurrentUser agent, Long id) {
        Reservation r = pending(id);
        Departure d = departures.findById(r.getDepartureId()).orElseThrow();
        long days = daysBefore(d);
        if (days < MIN_DAYS_BEFORE_DEPARTURE) {
            throw new BusinessException("ANTELACION_INSUFICIENTE",
                    "Faltan " + days + " días para la salida; se necesitan al menos " + MIN_DAYS_BEFORE_DEPARTURE,
                    Map.of("departureDate", d.getDepartureDate().toString()));
        }
        if (!availability.existsByDepartureIdAndConfirmedTrue(d.getId())) {
            throw new BusinessException("SIN_DISPONIBILIDAD",
                    "Ningún hotel tiene disponibilidad confirmada por el proveedor para esa salida",
                    Map.of("departureDate", d.getDepartureDate().toString()));
        }
        r.confirm(agent.username());
        reservations.save(r);
        audit.recordStatusChange(r.getCode(), "CONFIRMAR", ReservationStatus.PENDIENTE.name(),
                ReservationStatus.CONFIRMADA.name(), agent.username(), "Cumple cupo, antelación y disponibilidad");
        events.publishEvent(new NotificationRequested(r.getClientUsername(),
                "Tu reserva " + r.getCode() + " fue confirmada",
                "Tu reserva para la salida del " + d.getDepartureDate() + " quedó CONFIRMADA. Pronto te avisaremos el hotel y el tour.",
                r.getCode()));
        return r;
    }

    /** REQ-VIA-008. El rechazo libera las plazas que la solicitud ocupaba (RN-02). */
    @Transactional
    public Reservation reject(CurrentUser agent, Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("MOTIVO_OBLIGATORIO", "Escribe el motivo del rechazo",
                    Map.of("reason", "Es obligatorio"));
        }
        Reservation r = pending(id);
        r.reject(agent.username(), reason.trim());
        reservations.save(r);
        departures.releaseSeats(r.getDepartureId(), r.getTravelers());
        audit.recordStatusChange(r.getCode(), "RECHAZAR", ReservationStatus.PENDIENTE.name(),
                ReservationStatus.RECHAZADA.name(), agent.username(), reason.trim());
        events.publishEvent(new NotificationRequested(r.getClientUsername(),
                "Tu reserva " + r.getCode() + " no fue aprobada",
                "Motivo: " + reason.trim(), r.getCode()));
        return r;
    }
}
