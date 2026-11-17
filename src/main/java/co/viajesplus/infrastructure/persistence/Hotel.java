package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Hotel de un destino y el proveedor que confirma su disponibilidad (HU-20, RN-05). */
@Entity
@Table(name = "hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(name = "provider_username", nullable = false)
    private String providerUsername;

    protected Hotel() {
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDestination() {
        return destination;
    }

    public String getProviderUsername() {
        return providerUsername;
    }
}
