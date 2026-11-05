package co.viajesplus.infrastructure.config;

import co.viajesplus.domain.Role;
import co.viajesplus.infrastructure.persistence.AppUser;
import co.viajesplus.infrastructure.persistence.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Crea un usuario de prueba por rol en el proveedor OIDC simulado (IMP-01). Los nombres son ficticios
 * (Constitución 6 y Ley 1581: nunca datos personales reales en pruebas). La clave sale de VIAJESPLUS_DEMO_PASSWORD.
 */
@Component
public class DemoUsers implements ApplicationRunner {

    public record Demo(String username, String fullName, Role role) {
    }

    public static final List<Demo> USERS = List.of(
            new Demo("cliente@viajesplus.co", "Laura Gómez (cliente)", Role.CLIENTE),
            new Demo("cliente2@viajesplus.co", "Andrés Pérez (cliente)", Role.CLIENTE),
            new Demo("asesor@viajesplus.co", "Camila Ríos (asesora)", Role.ASESOR),
            new Demo("agente@viajesplus.co", "Julián Torres (agente)", Role.AGENTE),
            new Demo("proveedor@viajesplus.co", "Hotel Mar Azul (proveedor)", Role.PROVEEDOR),
            new Demo("coordinador@viajesplus.co", "Sofía Herrera (coordinadora)", Role.COORDINADOR),
            new Demo("guia@viajesplus.co", "Mateo Salazar (guía)", Role.GUIA),
            new Demo("conductor@viajesplus.co", "Diego Vargas (conductor)", Role.CONDUCTOR),
            new Demo("contador@viajesplus.co", "Paula Medina (contadora)", Role.CONTADOR),
            new Demo("gerente@viajesplus.co", "Ricardo León (gerente)", Role.GERENTE),
            new Demo("admin@viajesplus.co", "Administración ViajesPlus", Role.ADMIN));

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String password;

    public DemoUsers(AppUserRepository users, PasswordEncoder encoder,
                     @Value("${viajesplus.demo-password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String hash = null;
        for (Demo d : USERS) {
            if (users.findByUsernameIgnoreCase(d.username()).isEmpty()) {
                if (hash == null) {
                    hash = encoder.encode(password);
                }
                users.save(new AppUser(d.username(), d.fullName(), d.role(), hash));
            }
        }
    }
}
