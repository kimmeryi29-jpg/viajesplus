package co.viajesplus.api;

import co.viajesplus.application.ProviderService;
import co.viajesplus.infrastructure.persistence.HotelAvailability;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** HU-20 y HU-25 · El proveedor informa disponibilidad y tarifa de sus hoteles (RN-05). */
@RestController
@RequestMapping("/api/providers/availability")
@Tag(name = "Proveedor de hoteles")
public class ProviderController {

    public record AvailabilityView(Long hotelId, String hotelName, Long departureId, LocalDate departureDate,
                                   int rooms, BigDecimal ratePerNight, boolean confirmed) {
    }

    public record AvailabilityRequest(Long hotelId, Long departureId, Integer rooms, BigDecimal ratePerNight,
                                      Boolean confirmed) {
    }

    private final ProviderService providers;

    public ProviderController(ProviderService providers) {
        this.providers = providers;
    }

    @GetMapping
    @Operation(summary = "Disponibilidad de mis hoteles por salida", description = "Rol Proveedor.")
    public List<AvailabilityView> list(Authentication auth) {
        return providers.rowsFor(CurrentUsers.from(auth)).stream()
                .map(r -> new AvailabilityView(r.hotel().getId(), r.hotel().getName(), r.departure().getId(),
                        r.departure().getDepartureDate(), r.rooms(), r.ratePerNight(), r.confirmed()))
                .toList();
    }

    @PutMapping
    @Operation(summary = "Informar disponibilidad y tarifa", description = "Rol Proveedor. Un hotel de otro proveedor responde 403.")
    public AvailabilityView update(@RequestBody AvailabilityRequest body, Authentication auth) {
        HotelAvailability a = providers.update(CurrentUsers.from(auth), body.hotelId(), body.departureId(), body.rooms(),
                body.ratePerNight(), Boolean.TRUE.equals(body.confirmed()));
        return providers.rowsFor(CurrentUsers.from(auth)).stream()
                .filter(r -> r.hotel().getId().equals(a.getHotelId()) && r.departure().getId().equals(a.getDepartureId()))
                .findFirst()
                .map(r -> new AvailabilityView(r.hotel().getId(), r.hotel().getName(), r.departure().getId(),
                        r.departure().getDepartureDate(), r.rooms(), r.ratePerNight(), r.confirmed()))
                .orElse(new AvailabilityView(a.getHotelId(), null, a.getDepartureId(), null, a.getRooms(),
                        a.getRatePerNight(), a.isConfirmed()));
    }
}
