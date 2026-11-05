package co.viajesplus.web;

import co.viajesplus.api.CurrentUsers;
import co.viajesplus.application.AuditService;
import co.viajesplus.application.CatalogService;
import co.viajesplus.application.CurrentUser;
import co.viajesplus.application.NotFoundException;
import co.viajesplus.application.ReservationService;
import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.Quote;
import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.config.DemoUsers;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pantallas sencillas del Sprint 1: catálogo, cotización, solicitud de reserva, mis reservas y auditoría. */
@Controller
public class WebController {

    /** Lo que cada rol recibirá en los sprints siguientes (hoja Historias de Usuario). */
    private static final Map<Role, List<String>> PROXIMAMENTE = Map.of(
            Role.AGENTE, List.of("Bandeja de reservas pendientes (HU-16)", "Confirmar con cupo, antelación y disponibilidad (HU-17 a HU-20)", "Rechazar con motivo (HU-21)"),
            Role.PROVEEDOR, List.of("Informar disponibilidad y tarifa del hotel (HU-20)", "Solo hoteles con disponibilidad confirmada (HU-25)"),
            Role.COORDINADOR, List.of("Asignar hotel, guía y conductor sin cruces (HU-24 a HU-28)"),
            Role.GUIA, List.of("Aviso de salida asignada (HU-29)", "Itinerario y grupo de la salida, objetivo extendido (HU-42 a HU-45)"),
            Role.CONDUCTOR, List.of("Aviso con ruta y horario (HU-30)", "Ruta y horarios de sus salidas, objetivo extendido (HU-43)"),
            Role.CONTADOR, List.of("Reembolso registrado al cancelar, CR-001 (HU-36)", "Pagos y facturas, backlog futuro (HU-46 a HU-48)"));

    private final CatalogService catalog;
    private final ReservationService reservations;
    private final AuditService audit;
    private final DepartureRepository departures;
    private final String demoPassword;

    public WebController(CatalogService catalog, ReservationService reservations, AuditService audit,
                         DepartureRepository departures, @Value("${viajesplus.demo-password}") String demoPassword) {
        this.catalog = catalog;
        this.reservations = reservations;
        this.audit = audit;
        this.departures = departures;
        this.demoPassword = demoPassword;
    }

    @GetMapping("/")
    public String home(Model model) {
        Map<TravelPackage, List<Departure>> items = new LinkedHashMap<>();
        catalog.activePackages().forEach(p -> items.put(p, catalog.upcomingDepartures(p.getId())));
        model.addAttribute("items", items);
        return "inicio";
    }

    @GetMapping("/paquetes/{id}")
    public String paquete(@PathVariable Long id, @RequestParam(required = false) Integer viajeros, Model model) {
        TravelPackage p = catalog.getPackage(id);
        model.addAttribute("paquete", p);
        model.addAttribute("salidas", catalog.upcomingDepartures(id));
        int n = viajeros == null ? 2 : viajeros;
        model.addAttribute("viajeros", n);
        try {
            model.addAttribute("cotizacion", catalog.quote(id, n));
        } catch (BusinessException e) {
            model.addAttribute("errorCotizacion", e.getMessage());
        }
        return "paquete";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("usuarios", DemoUsers.USERS);
        model.addAttribute("claveDemo", demoPassword);
        return "login";
    }

    @GetMapping("/panel")
    public String panel(Authentication auth, Model model) {
        CurrentUser user = CurrentUsers.from(auth);
        switch (user.role()) {
            case CLIENTE:
                return "redirect:/reservas";
            case ASESOR:
                return "redirect:/asesor/cotizar";
            case GERENTE:
            case ADMIN:
                return "redirect:/auditoria";
            default:
                model.addAttribute("rol", user.role().label());
                model.addAttribute("proximamente", PROXIMAMENTE.getOrDefault(user.role(), List.of()));
                return "panel";
        }
    }

    @GetMapping("/reservas")
    public String misReservas(Authentication auth, Model model) {
        List<Reservation> lista = reservations.visibleTo(CurrentUsers.from(auth));
        model.addAttribute("reservas", lista);
        model.addAttribute("nombres", nombresPaquete());
        model.addAttribute("fechas", fechasSalida());
        return "reservas";
    }

    @PostMapping("/reservas")
    public String solicitar(@RequestParam Long paqueteId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                            @RequestParam(required = false) Integer viajeros,
                            Authentication auth, RedirectAttributes flash) {
        try {
            Reservation r = reservations.request(CurrentUsers.from(auth),
                    new ReservationService.RequestCommand(paqueteId, salida, viajeros));
            flash.addFlashAttribute("creada", r.getCode());
            return "redirect:/reservas";
        } catch (BusinessException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/paquetes/" + paqueteId + (viajeros == null ? "" : "?viajeros=" + viajeros);
        }
    }

    @GetMapping("/asesor/cotizar")
    public String cotizar(@RequestParam(required = false) Long paqueteId,
                          @RequestParam(required = false) Integer viajeros, Model model) {
        model.addAttribute("paquetes", catalog.activePackages());
        model.addAttribute("paqueteId", paqueteId);
        model.addAttribute("viajeros", viajeros == null ? 2 : viajeros);
        if (paqueteId != null && viajeros != null) {
            try {
                Quote q = catalog.quote(paqueteId, viajeros);
                model.addAttribute("cotizacion", q);
                model.addAttribute("paquete", catalog.getPackage(paqueteId));
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "asesor";
    }

    @GetMapping("/auditoria")
    public String auditoria(Model model) {
        model.addAttribute("registros", audit.latest());
        return "auditoria";
    }

    @GetMapping("/acceso-denegado")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String denegado() {
        return "acceso-denegado";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(Model model, NotFoundException e) {
        model.addAttribute("mensaje", e.getMessage());
        return "no-encontrado";
    }

    private Map<Long, String> nombresPaquete() {
        Map<Long, String> m = new LinkedHashMap<>();
        catalog.activePackages().forEach(p -> m.put(p.getId(), p.getName()));
        return m;
    }

    private Map<Long, LocalDate> fechasSalida() {
        Map<Long, LocalDate> m = new LinkedHashMap<>();
        departures.findAll().forEach(d -> m.put(d.getId(), d.getDepartureDate()));
        return m;
    }
}
