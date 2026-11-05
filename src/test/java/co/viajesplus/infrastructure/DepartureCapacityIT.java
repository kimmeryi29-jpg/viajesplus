package co.viajesplus.infrastructure;

import co.viajesplus.IntegrationTest;
import co.viajesplus.infrastructure.persistence.DepartureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** T003 · Restricción de cupo en la base de datos. REQ-VIA-005 · RN-03 · ADR-002 · AC-005.1 y AC-005.2. */
class DepartureCapacityIT extends IntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DepartureRepository departures;

    @Autowired
    TransactionTemplate tx;

    private long salidaConCupo(int cupo, String codigo) {
        jdbc.update("INSERT INTO travel_package (code, name, destination, description, days, price_per_person) "
                + "VALUES (?, 'Prueba', 'Prueba', 'Paquete de prueba', 2, 100000)", codigo);
        Long paquete = jdbc.queryForObject("SELECT id FROM travel_package WHERE code = ?", Long.class, codigo);
        jdbc.update("INSERT INTO departure (package_id, departure_date, capacity) VALUES (?, DATE '2027-06-01', ?)",
                paquete, cupo);
        return jdbc.queryForObject("SELECT id FROM departure WHERE package_id = ?", Long.class, paquete);
    }

    @Test
    @DisplayName("AC-005.1: dos operaciones simultáneas por la última plaza: solo una la ocupa")
    void ultimaPlazaSimultanea() throws Exception {
        long id = salidaConCupo(1, "PRB-01");
        CountDownLatch salida = new CountDownLatch(1);
        Callable<Integer> ocupar = () -> {
            salida.await();
            return tx.execute(s -> departures.occupySeats(id, 1));
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> a = pool.submit(ocupar);
            Future<Integer> b = pool.submit(ocupar);
            salida.countDown();
            assertThat(a.get() + b.get()).isEqualTo(1);
        } finally {
            pool.shutdown();
        }
        Integer ocupadas = jdbc.queryForObject("SELECT occupied_seats FROM departure WHERE id = ?", Integer.class, id);
        assertThat(ocupadas).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-005.2: la base de datos rechaza ocupar más plazas que el cupo aunque se salte el servicio")
    void restriccionEnLaBase() {
        long id = salidaConCupo(2, "PRB-02");
        assertThatThrownBy(() -> jdbc.update("UPDATE departure SET occupied_seats = 3 WHERE id = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(tx.execute(s -> departures.occupySeats(id, 3))).isZero();
        assertThat(tx.execute(s -> departures.occupySeats(id, 2))).isEqualTo(1);
    }
}
