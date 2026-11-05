package co.viajesplus.domain;

/** Los 10 roles del sistema (REQ-VIA-012, NFR-VIA-002). */
public enum Role {
    CLIENTE("Cliente"),
    ASESOR("Asesor de viajes"),
    AGENTE("Agente de reservas"),
    PROVEEDOR("Proveedor de hoteles"),
    COORDINADOR("Coordinador de tours"),
    GUIA("Guía turístico"),
    CONDUCTOR("Conductor"),
    CONTADOR("Contador"),
    GERENTE("Gerente"),
    ADMIN("Administrador");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
