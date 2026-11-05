package co.viajesplus.api;

import co.viajesplus.application.CatalogService;
import co.viajesplus.application.ReservationService;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** T008 · POST /api/reservations y consultas. HU-13, HU-14 y HU-15 · REQ-VIA-003, REQ-VIA-012. */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservas")
public class ReservationController {

    public record ReservationRequest(Long packageId, LocalDate departureDate, Integer travelers) {
    }

    public record ReservationView(Long id, String code, Long packageId, String packageName, LocalDate departureDate,
                                  int travelers, BigDecimal pricePerPerson, BigDecimal total, String status,
                                  String clientUsername, Instant createdAt) {
    }

    private final ReservationService reservations;
    private final CatalogService catalog;
    private final DepartureRepository departures;

    public ReservationController(ReservationService reservations, CatalogService catalog,
                                 DepartureRepository departures) {
        this.reservations = reservations;
        this.catalog = catalog;
        this.departures = departures;
    }

    ReservationView view(Reservation r) {
        LocalDate date = departures.findById(r.getDepartureId()).map(Departure::getDepartureDate).orElse(null);
        return new ReservationView(r.getId(), r.getCode(), r.getPackageId(), catalog.getPackage(r.getPackageId()).getName(),
                date, r.getTravelers(), r.getPricePerPerson(), r.getTotal(), r.getStatus().name(),
                r.getClientUsername(), r.getCreatedAt());
    }

    @PostMapping
    @Operation(summary = "Solicitar una reserva", description = "Rol Cliente. La reserva queda PENDIENTE (REQ-VIA-003).")
    public ResponseEntity<ReservationView> create(@RequestBody ReservationRequest body, Authentication auth) {
        Reservation r = reservations.request(CurrentUsers.from(auth),
                new ReservationService.RequestCommand(body.packageId(), body.departureDate(), body.travelers()));
        return ResponseEntity.created(URI.create("/api/reservations/" + r.getId())).body(view(r));
    }

    @GetMapping
    @Operation(summary = "Listar reservas", description = "El cliente ve solo las suyas (REQ-VIA-012).")
    public List<ReservationView> list(Authentication auth) {
        return reservations.visibleTo(CurrentUsers.from(auth)).stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una reserva", description = "La reserva de otro cliente responde 403.")
    public ReservationView get(@PathVariable Long id, Authentication auth) {
        return view(reservations.get(CurrentUsers.from(auth), id));
    }
}
