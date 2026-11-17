package co.viajesplus.api;

import co.viajesplus.application.AssignmentService;
import co.viajesplus.application.ReservationService;
import co.viajesplus.domain.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Opciones para la asignación del coordinador (HU-24 a HU-27). */
@RestController
@RequestMapping("/api/reservations/{id}/assignment-options")
@Tag(name = "Reservas")
public class HotelController {

    public record Option(String id, String name) {
    }

    public record AssignmentOptions(List<Option> hotels, List<Option> guides, List<Option> drivers) {
    }

    private final ReservationService reservations;
    private final AssignmentService assignments;

    public HotelController(ReservationService reservations, AssignmentService assignments) {
        this.reservations = reservations;
        this.assignments = assignments;
    }

    @GetMapping
    @Operation(summary = "Hoteles con disponibilidad confirmada, guías y conductores", description = "Rol Coordinador.")
    public AssignmentOptions options(@PathVariable Long id, Authentication auth) {
        var r = reservations.get(CurrentUsers.from(auth), id);
        return new AssignmentOptions(
                assignments.availableHotels(r).stream().map(h -> new Option(h.getId().toString(), h.getName())).toList(),
                assignments.staff(Role.GUIA).stream().map(u -> new Option(u.getUsername(), u.getFullName())).toList(),
                assignments.staff(Role.CONDUCTOR).stream().map(u -> new Option(u.getUsername(), u.getFullName())).toList());
    }
}
