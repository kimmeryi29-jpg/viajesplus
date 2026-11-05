package co.viajesplus.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "travel_package")
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private short days;

    @Column(name = "price_per_person", nullable = false)
    private BigDecimal pricePerPerson;

    @Column(nullable = false)
    private boolean active = true;

    protected TravelPackage() {
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

    public String getDescription() {
        return description;
    }

    public int getDays() {
        return days;
    }

    public BigDecimal getPricePerPerson() {
        return pricePerPerson;
    }

    public boolean isActive() {
        return active;
    }
}
