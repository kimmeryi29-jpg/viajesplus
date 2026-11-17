package co.viajesplus.api;

import co.viajesplus.application.AssignmentService;
import co.viajesplus.application.CatalogService;
import co.viajesplus.application.CurrentUser;
import co.viajesplus.application.DecisionService;
import co.viajesplus.application.ReservationService;
import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.ReservationStatus;
import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Hotel;
import co.viajesplus.infrastructure.persistence.HotelRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Reservas. Sprint 1 (T008): solicitud y consultas, HU-13 a HU-15. Sprint 2 (T011 y T015): confirmar, rechazar y
 * asignar, HU-16 a HU-30 · REQ-VIA-003 a REQ-VIA-009 y REQ-VIA-012.
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservas")
public class ReservationController {

    public record ReservationRequest(Long packageId, LocalDate departureDate, Integer travelers) {
    }

    public record RejectRequest(String reason) {
    }

    public record AssignmentRequest(Long hotelId, String guideUsername, String driverUsername) {
    }

    public record ReservationView(Long id, String code, Long packageId, String packageName, LocalDate departureDate,
                                  int travelers, BigDecimal pricePerPerson, BigDecimal total, String status,
                                  String clientUsername, Instant createdAt, String decidedBy, Instant decidedAt,
                                  String rejectionReason, String hotelName, String guideUsername,
                                  String driverUsername) {
    }

    private final ReservationService reservations;
    private final DecisionService decisions;
    private final AssignmentService assignments;
    private final CatalogService catalog;
    private final DepartureRepository departures;
    private final HotelRepository hotels;

    public ReservationController(ReservationService reservations, DecisionService decisions,
                                 AssignmentService assignments, CatalogService catalog,
                                 DepartureRepository departures, HotelRepository hotels) {
        this.reservations = reservations;
        this.decisions = decisions;
        this.assignments = assignments;
        this.catalog = catalog;
        this.departures = departures;
        this.hotels = hotels;
    }

    ReservationView view(Reservation r) {
        LocalDate date = departures.findById(r.getDepartureId()).map(Departure::getDepartureDate).orElse(null);
        String hotel = r.getHotelId() == null ? null : hotels.findById(r.getHotelId()).map(Hotel::getName).orElse(null);
        return new ReservationView(r.getId(), r.getCode(), r.getPackageId(), catalog.getPackage(r.getPackageId()).getName(),
                date, r.getTravelers(), r.getPricePerPerson(), r.getTotal(), r.getStatus().name(),
                r.getClientUsername(), r.getCreatedAt(), r.getDecidedBy(), r.getDecidedAt(), r.getRejectionReason(),
                hotel, r.getGuideUsername(), r.getDriverUsername());
    }

    @PostMapping
    @Operation(summary = "Solicitar una reserva",
            description = "Rol Cliente. Queda PENDIENTE y ocupa cupo (RN-02); sin plazas responde CUPO_INSUFICIENTE.")
    public ResponseEntity<ReservationView> create(@RequestBody ReservationRequest body, Authentication auth) {
        Reservation r = reservations.request(CurrentUsers.from(auth),
                new ReservationService.RequestCommand(body.packageId(), body.departureDate(), body.travelers()));
        return ResponseEntity.created(URI.create("/api/reservations/" + r.getId())).body(view(r));
    }

    @GetMapping
    @Operation(summary = "Listar reservas",
            description = "El cliente ve solo las suyas (REQ-VIA-012). Con status=PENDIENTE, el agente recibe su bandeja "
                    + "ordenada por fecha de salida (HU-16).")
    public List<ReservationView> list(@RequestParam(required = false) String status, Authentication auth) {
        CurrentUser user = CurrentUsers.from(auth);
        if (status == null || status.isBlank()) {
            return reservations.visibleTo(user).stream().map(this::view).toList();
        }
        ReservationStatus wanted;
        try {
            wanted = ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw BusinessException.invalidField("status", "Estado no válido");
        }
        List<Reservation> base = wanted == ReservationStatus.PENDIENTE && user.role() == Role.AGENTE
                ? reservations.pendingByDeparture()
                : reservations.visibleTo(user).stream().filter(r -> r.getStatus() == wanted).toList();
        return base.stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una reserva", description = "La reserva de otro cliente responde 403.")
    public ReservationView get(@PathVariable Long id, Authentication auth) {
        return view(reservations.get(CurrentUsers.from(auth), id));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmar una reserva (REQ-VIA-006, 007)",
            description = "Rol Agente. 422 ANTELACION_INSUFICIENTE, SIN_DISPONIBILIDAD o ESTADO_INVALIDO.")
    public ReservationView confirm(@PathVariable Long id, Authentication auth) {
        return view(decisions.confirm(CurrentUsers.from(auth), id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar una reserva (REQ-VIA-008)", description = "Rol Agente. Sin motivo: 422 MOTIVO_OBLIGATORIO.")
    public ReservationView reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest body,
                                  Authentication auth) {
        return view(decisions.reject(CurrentUsers.from(auth), id, body == null ? null : body.reason()));
    }

    @PutMapping("/{id}/assignment")
    @Operation(summary = "Asignar hotel, guía y conductor (REQ-VIA-009)",
            description = "Rol Coordinador. 422 HOTEL_SIN_DISPONIBILIDAD, CRUCE_GUIA, CRUCE_CONDUCTOR o ESTADO_INVALIDO.")
    public ReservationView assign(@PathVariable Long id, @RequestBody AssignmentRequest body, Authentication auth) {
        return view(assignments.assign(CurrentUsers.from(auth), id,
                new AssignmentService.AssignCommand(body.hotelId(), body.guideUsername(), body.driverUsername())));
    }
}
