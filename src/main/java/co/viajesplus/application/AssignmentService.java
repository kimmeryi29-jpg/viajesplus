package co.viajesplus.application;

import co.viajesplus.domain.BusinessException;
import co.viajesplus.domain.ReservationStatus;
import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.persistence.AppUser;
import co.viajesplus.infrastructure.persistence.AppUserRepository;
import co.viajesplus.infrastructure.persistence.Departure;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import co.viajesplus.infrastructure.persistence.Hotel;
import co.viajesplus.infrastructure.persistence.HotelAvailability;
import co.viajesplus.infrastructure.persistence.HotelAvailabilityRepository;
import co.viajesplus.infrastructure.persistence.HotelRepository;
import co.viajesplus.infrastructure.persistence.Reservation;
import co.viajesplus.infrastructure.persistence.ReservationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * T014 · Caso de uso AssignHotelAndTour. HU3: el coordinador asigna hotel, guía y conductor a una reserva CONFIRMADA.
 * Trazabilidad: REQ-VIA-009, REQ-VIA-011 · RN-05 (hotel con disponibilidad confirmada) · RN-06 (sin cruces el mismo día)
 * · AC-009.1 a AC-009.3. Avisa al guía y al conductor (HU-28 y HU-29, ADR-004).
 */
@Service
public class AssignmentService {

    private final ReservationRepository reservations;
    private final DepartureRepository departures;
    private final HotelRepository hotels;
    private final HotelAvailabilityRepository availability;
    private final AppUserRepository users;
    private final AuditService audit;
    private final ApplicationEventPublisher events;

    public AssignmentService(ReservationRepository reservations, DepartureRepository departures, HotelRepository hotels,
                             HotelAvailabilityRepository availability, AppUserRepository users, AuditService audit,
                             ApplicationEventPublisher events) {
        this.reservations = reservations;
        this.departures = departures;
        this.hotels = hotels;
        this.availability = availability;
        this.users = users;
        this.audit = audit;
        this.events = events;
    }

    public record AssignCommand(Long hotelId, String guideUsername, String driverUsername) {
    }

    /** Hoteles del destino con disponibilidad confirmada para la salida de la reserva. */
    @Transactional(readOnly = true)
    public List<Hotel> availableHotels(Reservation r) {
        return availability.findByDepartureId(r.getDepartureId()).stream()
                .filter(HotelAvailability::isConfirmed)
                .map(a -> hotels.findById(a.getHotelId()).orElseThrow())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppUser> staff(Role role) {
        return users.findByRoleOrderByFullNameAsc(role);
    }

    private void requireRole(String username, Role role, String field) {
        AppUser u = users.findByUsernameIgnoreCase(username == null ? "" : username)
                .orElseThrow(() -> BusinessException.invalidField(field, "Elige un " + role.label().toLowerCase()));
        if (u.getRole() != role) {
            throw BusinessException.invalidField(field, "El usuario no es " + role.label().toLowerCase());
        }
    }

    @Transactional
    public Reservation assign(CurrentUser coordinator, Long reservationId, AssignCommand cmd) {
        Reservation r = reservations.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("No existe la reserva " + reservationId));
        if (r.getStatus() != ReservationStatus.CONFIRMADA) {
            throw new BusinessException("ESTADO_INVALIDO", "Solo se asigna a una reserva CONFIRMADA; esta está "
                    + r.getStatus(), Map.of("status", r.getStatus().name()));
        }
        if (cmd.hotelId() == null) {
            throw BusinessException.invalidField("hotelId", "Elige un hotel");
        }
        requireRole(cmd.guideUsername(), Role.GUIA, "guideUsername");
        requireRole(cmd.driverUsername(), Role.CONDUCTOR, "driverUsername");
        Departure d = departures.findById(r.getDepartureId()).orElseThrow();
        Hotel hotel = hotels.findById(cmd.hotelId())
                .orElseThrow(() -> BusinessException.invalidField("hotelId", "No existe el hotel"));
        boolean confirmed = availability.findByHotelIdAndDepartureId(hotel.getId(), d.getId())
                .map(HotelAvailability::isConfirmed).orElse(false);
        if (!confirmed) {
            throw new BusinessException("HOTEL_SIN_DISPONIBILIDAD",
                    "El proveedor no ha confirmado disponibilidad de " + hotel.getName() + " para esa salida",
                    Map.of("hotelId", hotel.getName()));
        }
        if (reservations.countGuideConflicts(cmd.guideUsername(), d.getDepartureDate(), d.getId()) > 0) {
            throw new BusinessException("CRUCE_GUIA", "El guía ya tiene otra salida ese día",
                    Map.of("guideUsername", d.getDepartureDate().toString()));
        }
        if (reservations.countDriverConflicts(cmd.driverUsername(), d.getDepartureDate(), d.getId()) > 0) {
            throw new BusinessException("CRUCE_CONDUCTOR", "El conductor ya tiene otra salida ese día",
                    Map.of("driverUsername", d.getDepartureDate().toString()));
        }
        r.assign(hotel.getId(), cmd.guideUsername().toLowerCase(), cmd.driverUsername().toLowerCase());
        reservations.save(r);
        audit.recordStatusChange(r.getCode(), "ASIGNAR", r.getStatus().name(), r.getStatus().name(),
                coordinator.username(), "Hotel " + hotel.getName() + ", guía y conductor asignados");
        String fecha = d.getDepartureDate().toString();
        events.publishEvent(new NotificationRequested(r.getGuideUsername(), "Nueva salida asignada: " + fecha,
                "Te asignaron el grupo de la reserva " + r.getCode() + " (" + r.getTravelers() + " viajeros), hotel "
                        + hotel.getName() + ".", r.getCode()));
        events.publishEvent(new NotificationRequested(r.getDriverUsername(), "Nuevo traslado asignado: " + fecha,
                "Recoge al grupo de la reserva " + r.getCode() + " el " + fecha + " a las 7:00 a. m. en el punto de "
                        + "encuentro de la agencia, con destino a " + hotel.getName() + ".", r.getCode()));
        events.publishEvent(new NotificationRequested(r.getClientUsername(), "Hotel y tour de tu reserva " + r.getCode(),
                "Tu alojamiento será " + hotel.getName() + ". El guía y el transporte ya están asignados.", r.getCode()));
        return r;
    }
}
