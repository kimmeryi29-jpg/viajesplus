package co.viajesplus.api;

import co.viajesplus.application.CurrentUser;
import co.viajesplus.domain.Role;
import org.springframework.security.core.Authentication;

/** Traduce la autenticación (token OIDC o sesión web) al usuario de la aplicación. */
public final class CurrentUsers {

    private CurrentUsers() {
    }

    public static CurrentUser from(Authentication auth) {
        Role role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> Role.valueOf(a.substring(5)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuario sin rol"));
        return new CurrentUser(auth.getName(), role);
    }
}
