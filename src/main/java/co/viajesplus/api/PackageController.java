package co.viajesplus.api;

import co.viajesplus.application.CatalogService;
import co.viajesplus.domain.Quote;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
@Tag(name = "Paquetes y cotización")
public class PackageController {

    public record DepartureView(LocalDate date, int capacity, int availableSeats) {
        static DepartureView of(Departure d) {
            return new DepartureView(d.getDepartureDate(), d.getCapacity(), d.getAvailableSeats());
        }
    }

    public record PackageView(Long id, String code, String name, String destination, String description, int days,
                              BigDecimal pricePerPerson, List<DepartureView> departures) {
    }

    public record QuoteView(Long packageId, String packageName, BigDecimal pricePerPerson, int travelers,
                            BigDecimal total) {
    }

    private final CatalogService catalog;

    public PackageController(CatalogService catalog) {
        this.catalog = catalog;
    }

    private PackageView view(TravelPackage p) {
        return new PackageView(p.getId(), p.getCode(), p.getName(), p.getDestination(), p.getDescription(),
                p.getDays(), p.getPricePerPerson(),
                catalog.upcomingDepartures(p.getId()).stream().map(DepartureView::of).toList());
    }

    @GetMapping
    @Operation(summary = "Listar paquetes activos con sus próximas salidas")
    public List<PackageView> list() {
        return catalog.activePackages().stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un paquete")
    public PackageView get(@PathVariable Long id) {
        return view(catalog.getPackage(id));
    }

    /** REQ-VIA-001 y 002 · AC-001.1, AC-002.1 a 002.3 · HU-11 y HU-12. */
    @GetMapping("/{id}/quote")
    @Operation(summary = "Cotizar un paquete", description = "Total = tarifa por persona × viajeros (RN-01). Roles: Cliente, Asesor y Agente.")
    public QuoteView quote(@PathVariable Long id, @RequestParam int travelers) {
        TravelPackage p = catalog.getPackage(id);
        Quote q = catalog.quote(id, travelers);
        return new QuoteView(p.getId(), p.getName(), q.pricePerPerson(), q.travelers(), q.total());
    }
}
