package co.viajesplus.application;

import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.Quote;
import co.viajesplus.domain.QuoteCalculator;
import co.viajesplus.domain.ReservationStatus;
import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import co.viajesplus.infrastructure.persistence.ReservationRepository;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * T008 · ReservationService. HU1: el cliente solicita una reserva indicando paquete, fecha de salida y viajeros.
 * Trazabilidad: REQ-VIA-001, REQ-VIA-003, REQ-VIA-011, REQ-VIA-012 · AC-001.1, AC-003.1, AC-003.2 · RN-01.
 * En el Sprint 1 la solicitud no ocupa cupo: el cupo se valida y se ocupa al confirmar (Sprint 2, PBI-04).
 */
@Service
public class ReservationService {

    /** Roles que pueden consultar todas las reservas; el cliente solo ve las suyas (REQ-VIA-012). */
    private static final Set<Role> SEE_ALL = EnumSet.of(Role.ASESOR, Role.AGENTE, Role.GERENTE, Role.ADMIN);

    private final CatalogService catalog;
    private final DepartureRepository departures;
    private final ReservationRepository reservations;
    private final AuditService audit;
    private final Clock clock;

    public ReservationService(CatalogService catalog, DepartureRepository departures,
                              ReservationRepository reservations, AuditService audit, Clock clock) {
        this.catalog = catalog;
        this.departures = departures;
        this.reservations = reservations;
        this.audit = audit;
        this.clock = clock;
    }

    public record RequestCommand(Long packageId, LocalDate departureDate, Integer travelers) {
    }

    @Transactional
    public Reservation request(CurrentUser user, RequestCommand cmd) {
        if (cmd.packageId() == null) {
            throw BusinessException.invalidField("packageId", "Selecciona un paquete");
        }
        if (cmd.departureDate() == null) {
            throw BusinessException.invalidField("departureDate", "Indica la fecha de salida");
        }
        if (cmd.travelers() == null) {
            throw BusinessException.invalidField("travelers", "Indica el número de viajeros");
        }
        TravelPackage pkg = catalog.getPackage(cmd.packageId());
        Departure departure = departures.findByPackageIdAndDepartureDate(pkg.getId(), cmd.departureDate())
                .orElseThrow(() -> BusinessException.invalidField("departureDate",
                        "El paquete no tiene salida programada en esa fecha"));
        if (!departure.getDepartureDate().isAfter(LocalDate.now(clock))) {
            throw BusinessException.invalidField("departureDate", "La fecha de salida ya pasó");
        }
        Quote quote = QuoteCalculator.calculate(pkg.getPricePerPerson(), cmd.travelers());
        String code = "RES-%05d".formatted(reservations.nextCodeNumber());
        Reservation saved = reservations.save(new Reservation(code, user.username(), pkg.getId(), departure.getId(),
                quote.travelers(), quote.pricePerPerson(), quote.total()));
        audit.recordStatusChange(code, "SOLICITAR", null, ReservationStatus.PENDIENTE.name(), user.username(),
                "Solicitud del cliente");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Reservation> visibleTo(CurrentUser user) {
        if (SEE_ALL.contains(user.role())) {
            return reservations.findAllByOrderByCreatedAtDesc();
        }
        return reservations.findByClientUsernameOrderByCreatedAtDesc(user.username());
    }

    @Transactional(readOnly = true)
    public Reservation get(CurrentUser user, Long id) {
        Reservation r = reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la reserva " + id));
        if (!SEE_ALL.contains(user.role()) && !r.getClientUsername().equalsIgnoreCase(user.username())) {
            throw new ForbiddenException("La reserva pertenece a otro cliente");
        }
        return r;
    }
}
