package co.viajesplus;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Datos de prueba independientes por caso: cada salida usa un paquete y un destino propios. */
public final class TestData {

    /** Fecha fija de las pruebas (IntegrationTest). */
    public static final LocalDate HOY = LocalDate.of(2026, 11, 5);

    private static final AtomicInteger SEQ = new AtomicInteger();

    public record Salida(long packageId, long departureId, LocalDate date, String destination, long hotelId) {
    }

    private final JdbcTemplate jdbc;

    public TestData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Paquete con una salida a {@code dias} días de HOY, cupo dado y un hotel con o sin disponibilidad confirmada. */
    public Salida salida(int dias, int cupo, boolean hotelConfirmado) {
        int n = SEQ.incrementAndGet();
        String code = "T2-" + n;
        String destino = "Destino prueba " + n;
        LocalDate fecha = HOY.plusDays(dias);
        jdbc.update("INSERT INTO travel_package (code, name, destination, description, days, price_per_person) "
                + "VALUES (?, ?, ?, 'Paquete de prueba', 3, 1000000)", code, "Paquete " + code, destino);
        Long pkg = jdbc.queryForObject("SELECT id FROM travel_package WHERE code = ?", Long.class, code);
        jdbc.update("INSERT INTO departure (package_id, departure_date, capacity) VALUES (?, ?, ?)", pkg, fecha, cupo);
        Long dep = jdbc.queryForObject("SELECT id FROM departure WHERE package_id = ?", Long.class, pkg);
        long hotel = hotel(destino, dep, hotelConfirmado, "proveedor@viajesplus.co");
        return new Salida(pkg, dep, fecha, destino, hotel);
    }

    /** Hotel del destino con disponibilidad (confirmada o no) para la salida. */
    public long hotel(String destino, long departureId, boolean confirmado, String proveedor) {
        String code = "H2-" + SEQ.incrementAndGet();
        jdbc.update("INSERT INTO hotel (code, name, destination, provider_username) VALUES (?, ?, ?, ?)",
                code, "Hotel " + code, destino, proveedor);
        Long id = jdbc.queryForObject("SELECT id FROM hotel WHERE code = ?", Long.class, code);
        jdbc.update("INSERT INTO hotel_availability (hotel_id, departure_id, rooms, rate_per_night, confirmed, updated_by) "
                + "VALUES (?, ?, 10, 250000, ?, ?)", id, departureId, confirmado, proveedor);
        return id;
    }

    public int plazasOcupadas(long departureId) {
        return jdbc.queryForObject("SELECT occupied_seats FROM departure WHERE id = ?", Integer.class, departureId);
    }
}
