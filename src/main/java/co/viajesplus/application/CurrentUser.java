package co.viajesplus.application;

import co.viajesplus.domain.Role;

/** Usuario autenticado tal como llega del proveedor OIDC (simulado en el Sprint 1). */
public record CurrentUser(String username, Role role) {

    public boolean is(Role r) {
        return role == r;
    }
}
