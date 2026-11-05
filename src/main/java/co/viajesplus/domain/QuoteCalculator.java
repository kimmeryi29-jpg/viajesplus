package co.viajesplus.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * T007 · Regla de cotización RN-01: total = tarifa por persona × número de viajeros, en pesos con dos decimales.
 * Trazabilidad: REQ-VIA-002 · AC-002.1 a AC-002.3.
 */
public final class QuoteCalculator {

    private QuoteCalculator() {
    }

    public static Quote calculate(BigDecimal pricePerPerson, int travelers) {
        if (pricePerPerson == null || pricePerPerson.signum() <= 0) {
            throw BusinessException.invalidField("pricePerPerson", "La tarifa por persona debe ser mayor que cero");
        }
        if (travelers < 1) {
            throw BusinessException.invalidField("travelers", "Debe haber al menos un viajero");
        }
        BigDecimal price = pricePerPerson.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = price.multiply(BigDecimal.valueOf(travelers)).setScale(2, RoundingMode.HALF_UP);
        return new Quote(price, travelers, total);
    }
}
