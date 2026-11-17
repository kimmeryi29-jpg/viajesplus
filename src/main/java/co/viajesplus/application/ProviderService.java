package co.viajesplus.application;

import co.viajesplus.domain.BusinessException;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Hotel;
import co.viajesplus.infrastructure.persistence.HotelAvailability;
import co.viajesplus.infrastructure.persistence.HotelAvailabilityRepository;
import co.viajesplus.infrastructure.persistence.HotelRepository;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import co.viajesplus.infrastructure.persistence.TravelPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** HU-20 y HU-25 · El proveedor informa la disponibilidad y la tarifa de sus hoteles para cada salida (RN-05). */
@Service
public class ProviderService {

    public record Row(Hotel hotel, Departure departure, int rooms, BigDecimal ratePerNight, boolean confirmed) {
    }

    private final HotelRepository hotels;
    private final HotelAvailabilityRepository availability;
    private final DepartureRepository departures;
    private final TravelPackageRepository packages;
    private final Clock clock;

    public ProviderService(HotelRepository hotels, HotelAvailabilityRepository availability,
                           DepartureRepository departures, TravelPackageRepository packages, Clock clock) {
        this.hotels = hotels;
        this.availability = availability;
        this.departures = departures;
        this.packages = packages;
        this.clock = clock;
    }

    /** Las salidas futuras de los destinos de sus hoteles, con lo que el proveedor ya informó. */
    @Transactional(readOnly = true)
    public List<Row> rowsFor(CurrentUser provider) {
        LocalDate today = LocalDate.now(clock);
        List<Row> rows = new ArrayList<>();
        for (Hotel h : hotels.findByProviderUsernameIgnoreCaseOrderByNameAsc(provider.username())) {
            for (TravelPackage p : packages.findAll()) {
                if (!p.getDestination().equals(h.getDestination())) {
                    continue;
                }
                for (Departure d : departures.findByPackageIdOrderByDepartureDateAsc(p.getId())) {
                    if (!d.getDepartureDate().isAfter(today)) {
                        continue;
                    }
                    HotelAvailability a = availability.findByHotelIdAndDepartureId(h.getId(), d.getId()).orElse(null);
                    rows.add(new Row(h, d, a == null ? 0 : a.getRooms(),
                            a == null ? BigDecimal.ZERO : a.getRatePerNight(), a != null && a.isConfirmed()));
                }
            }
        }
        rows.sort(Comparator.comparing((Row r) -> r.departure().getDepartureDate()).thenComparing(r -> r.hotel().getName()));
        return rows;
    }

    @Transactional
    public HotelAvailability update(CurrentUser provider, Long hotelId, Long departureId, Integer rooms,
                                    BigDecimal ratePerNight, boolean confirmed) {
        Hotel h = hotels.findById(hotelId).orElseThrow(() -> new NotFoundException("No existe el hotel " + hotelId));
        if (!h.getProviderUsername().equalsIgnoreCase(provider.username())) {
            throw new ForbiddenException("El hotel pertenece a otro proveedor");
        }
        Departure d = departures.findById(departureId)
                .orElseThrow(() -> new NotFoundException("No existe la salida " + departureId));
        if (rooms == null || rooms < 0) {
            throw BusinessException.invalidField("rooms", "Indica las habitaciones disponibles (0 o más)");
        }
        if (ratePerNight == null || ratePerNight.signum() < 0) {
            throw BusinessException.invalidField("ratePerNight", "Indica la tarifa por noche");
        }
        if (confirmed && rooms == 0) {
            throw new BusinessException("DATOS_INVALIDOS", "No se puede confirmar disponibilidad con 0 habitaciones",
                    Map.of("rooms", "Debe ser mayor que 0 para confirmar"));
        }
        HotelAvailability a = availability.findByHotelIdAndDepartureId(h.getId(), d.getId())
                .orElseGet(() -> new HotelAvailability(h.getId(), d.getId()));
        a.update(rooms, ratePerNight, confirmed, provider.username());
        return availability.save(a);
    }
}
