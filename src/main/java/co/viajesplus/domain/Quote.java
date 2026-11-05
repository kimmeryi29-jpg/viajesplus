package co.viajesplus.domain;

import java.math.BigDecimal;

/** Resultado de una cotización: tarifa por persona, número de viajeros y total (RN-01). */
public record Quote(BigDecimal pricePerPerson, int travelers, BigDecimal total) {
}
