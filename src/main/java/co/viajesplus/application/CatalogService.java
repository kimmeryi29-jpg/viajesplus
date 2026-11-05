package co.viajesplus.application;

import co.viajesplus.domain.Quote;
import co.viajesplus.domain.QuoteCalculator;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import co.viajesplus.infrastructure.persistence.TravelPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Catálogo de paquetes y cotización (REQ-VIA-001, REQ-VIA-002). */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final TravelPackageRepository packages;
    private final DepartureRepository departures;
    private final Clock clock;

    public CatalogService(TravelPackageRepository packages, DepartureRepository departures, Clock clock) {
        this.packages = packages;
        this.departures = departures;
        this.clock = clock;
    }

    public List<TravelPackage> activePackages() {
        return packages.findByActiveTrueOrderByNameAsc();
    }

    public TravelPackage getPackage(Long id) {
        return packages.findById(id).filter(TravelPackage::isActive)
                .orElseThrow(() -> new NotFoundException("No existe el paquete " + id));
    }

    /** Salidas futuras del paquete, de la más próxima a la más lejana. */
    public List<Departure> upcomingDepartures(Long packageId) {
        LocalDate today = LocalDate.now(clock);
        return departures.findByPackageIdOrderByDepartureDateAsc(packageId).stream()
                .filter(d -> d.getDepartureDate().isAfter(today))
                .toList();
    }

    /** REQ-VIA-001: el cliente y el asesor ven la tarifa por persona y el total con la misma regla RN-01. */
    public Quote quote(Long packageId, int travelers) {
        return QuoteCalculator.calculate(getPackage(packageId).getPricePerPerson(), travelers);
    }
}
