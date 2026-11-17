package co.viajesplus.web;

import co.viajesplus.infrastructure.persistence.AppUser;
import co.viajesplus.infrastructure.persistence.AppUserRepository;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Hotel;
import co.viajesplus.infrastructure.persistence.HotelRepository;
import co.viajesplus.infrastructure.persistence.TravelPackage;
import co.viajesplus.infrastructure.persistence.TravelPackageRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Nombres legibles para las vistas: ${@nombres.paquete(id)}, ${@nombres.hotel(id)}, ${@nombres.usuario(correo)}. */
@Component("nombres")
public class Nombres {

    private final TravelPackageRepository packages;
    private final DepartureRepository departures;
    private final HotelRepository hotels;
    private final AppUserRepository users;

    public Nombres(TravelPackageRepository packages, DepartureRepository departures, HotelRepository hotels,
                   AppUserRepository users) {
        this.packages = packages;
        this.departures = departures;
        this.hotels = hotels;
        this.users = users;
    }

    public String paquete(Long id) {
        return id == null ? "" : packages.findById(id).map(TravelPackage::getName).orElse("");
    }

    public LocalDate salida(Long departureId) {
        return departureId == null ? null : departures.findById(departureId).map(Departure::getDepartureDate).orElse(null);
    }

    public Departure departure(Long departureId) {
        return departureId == null ? null : departures.findById(departureId).orElse(null);
    }

    public String hotel(Long id) {
        return id == null ? "" : hotels.findById(id).map(Hotel::getName).orElse("");
    }

    public String usuario(String username) {
        return username == null ? "" : users.findByUsernameIgnoreCase(username).map(AppUser::getFullName).orElse(username);
    }
}
