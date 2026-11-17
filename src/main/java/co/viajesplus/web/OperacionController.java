package co.viajesplus.web;

import co.viajesplus.api.CurrentUsers;
import co.viajesplus.application.AssignmentService;
import co.viajesplus.application.DecisionService;
import co.viajesplus.application.NotificationService;
import co.viajesplus.application.ProviderService;
import co.viajesplus.application.ReservationService;
import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.ReservationStatus;
import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.HotelAvailabilityRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pantallas del Sprint 2: bandeja del agente, disponibilidad del proveedor, asignación del coordinador y avisos. */
@Controller
public class OperacionController {

    private final ReservationService reservations;
    private final DecisionService decisions;
    private final ProviderService providers;
    private final AssignmentService assignments;
    private final NotificationService notifications;
    private final DepartureRepository departures;
    private final HotelAvailabilityRepository availability;

    public OperacionController(ReservationService reservations, DecisionService decisions, ProviderService providers,
                               AssignmentService assignments, NotificationService notifications,
                               DepartureRepository departures, HotelAvailabilityRepository availability) {
        this.reservations = reservations;
        this.decisions = decisions;
        this.providers = providers;
        this.assignments = assignments;
        this.notifications = notifications;
        this.departures = departures;
        this.availability = availability;
    }

    public record Pendiente(Reservation reserva, Departure salida, long dias, boolean disponibilidad) {
    }

    /** HU-16 a HU-22: bandeja del agente con días de antelación, plazas libres y disponibilidad del proveedor. */
    @GetMapping("/agente")
    public String agente(Model model) {
        List<Pendiente> filas = reservations.pendingByDeparture().stream().map(r -> {
            Departure d = departures.findById(r.getDepartureId()).orElseThrow();
            return new Pendiente(r, d, decisions.daysBefore(d), availability.existsByDepartureIdAndConfirmedTrue(d.getId()));
        }).toList();
        model.addAttribute("pendientes", filas);
        model.addAttribute("decididas", reservations.visibleTo(new co.viajesplus.application.CurrentUser("", Role.AGENTE))
                .stream().filter(r -> r.getStatus() != ReservationStatus.PENDIENTE).limit(15).toList());
        model.addAttribute("minimo", DecisionService.MIN_DAYS_BEFORE_DEPARTURE);
        return "agente";
    }

    @PostMapping("/agente/{id}/confirmar")
    public String confirmar(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        try {
            Reservation r = decisions.confirm(CurrentUsers.from(auth), id);
            flash.addFlashAttribute("ok", "Reserva " + r.getCode() + " confirmada. El cliente recibió el aviso.");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getCode() + ": " + e.getMessage());
        }
        return "redirect:/agente";
    }

    @PostMapping("/agente/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(required = false) String motivo, Authentication auth,
                           RedirectAttributes flash) {
        try {
            Reservation r = decisions.reject(CurrentUsers.from(auth), id, motivo);
            flash.addFlashAttribute("ok", "Reserva " + r.getCode() + " rechazada. Se liberó el cupo y se avisó al cliente.");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getCode() + ": " + e.getMessage());
        }
        return "redirect:/agente";
    }

    /** HU-20: el proveedor informa disponibilidad y tarifa por salida. */
    @GetMapping("/proveedor")
    public String proveedor(Authentication auth, Model model) {
        model.addAttribute("filas", providers.rowsFor(CurrentUsers.from(auth)));
        return "proveedor";
    }

    @PostMapping("/proveedor")
    public String guardarDisponibilidad(@RequestParam Long hotelId, @RequestParam Long departureId,
                                        @RequestParam(required = false) Integer habitaciones,
                                        @RequestParam(required = false) BigDecimal tarifa,
                                        @RequestParam(defaultValue = "false") boolean confirmada,
                                        Authentication auth, RedirectAttributes flash) {
        try {
            providers.update(CurrentUsers.from(auth), hotelId, departureId, habitaciones, tarifa, confirmada);
            flash.addFlashAttribute("ok", "Disponibilidad guardada.");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedor";
    }

    /** HU-24 a HU-28: el coordinador asigna hotel, guía y conductor a las reservas confirmadas. */
    @GetMapping("/coordinador")
    public String coordinador(Model model) {
        List<Reservation> confirmadas = reservations.withStatus(ReservationStatus.CONFIRMADA);
        Map<Long, Object> hoteles = new LinkedHashMap<>();
        confirmadas.forEach(r -> hoteles.put(r.getId(), assignments.availableHotels(r)));
        model.addAttribute("confirmadas", confirmadas);
        model.addAttribute("hoteles", hoteles);
        model.addAttribute("guias", assignments.staff(Role.GUIA));
        model.addAttribute("conductores", assignments.staff(Role.CONDUCTOR));
        return "coordinador";
    }

    @PostMapping("/coordinador/{id}/asignar")
    public String asignar(@PathVariable Long id, @RequestParam(required = false) Long hotelId,
                          @RequestParam(required = false) String guia, @RequestParam(required = false) String conductor,
                          Authentication auth, RedirectAttributes flash) {
        try {
            Reservation r = assignments.assign(CurrentUsers.from(auth), id,
                    new AssignmentService.AssignCommand(hotelId, guia, conductor));
            flash.addFlashAttribute("ok", "Reserva " + r.getCode() + " asignada. Se avisó al guía, al conductor y al cliente.");
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getCode() + ": " + e.getMessage());
        }
        return "redirect:/coordinador";
    }

    /** Avisos del usuario (T012, T015) y, para guía y conductor, sus salidas asignadas (HU-29, HU-30). */
    @GetMapping("/avisos")
    public String avisos(Authentication auth, Model model) {
        var user = CurrentUsers.from(auth);
        model.addAttribute("avisos", notifications.forUser(user.username()));
        if (user.role() == Role.GUIA || user.role() == Role.CONDUCTOR) {
            model.addAttribute("asignadas", reservations.visibleTo(user));
        }
        return "avisos";
    }
}
