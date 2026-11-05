package co.viajesplus.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** T006 · Pruebas de QuoteCalculator, escritas antes de T007. AC-002.1 a AC-002.3 · REQ-VIA-002 · RN-01. */
class QuoteCalculatorTest {

    @Test
    @DisplayName("AC-002.1: con $1.200.000 por persona y 3 viajeros el total es $3.600.000")
    void tresViajeros() {
        Quote q = QuoteCalculator.calculate(new BigDecimal("1200000"), 3);
        assertThat(q.total()).isEqualByComparingTo("3600000");
        assertThat(q.pricePerPerson()).isEqualByComparingTo("1200000");
        assertThat(q.travelers()).isEqualTo(3);
    }

    @Test
    @DisplayName("AC-002.2 (borde): con 1 viajero el total es la tarifa por persona")
    void unViajero() {
        Quote q = QuoteCalculator.calculate(new BigDecimal("650000"), 1);
        assertThat(q.total()).isEqualByComparingTo("650000");
    }

    @Test
    @DisplayName("AC-002.3 (error): con 0 viajeros se rechaza con DATOS_INVALIDOS en el campo travelers")
    void ceroViajeros() {
        assertThatThrownBy(() -> QuoteCalculator.calculate(new BigDecimal("1200000"), 0))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException b = (BusinessException) e;
                    assertThat(b.getCode()).isEqualTo("DATOS_INVALIDOS");
                    assertThat(b.getDetails()).containsKey("travelers");
                });
    }

    @Test
    @DisplayName("El total se redondea a dos decimales")
    void dosDecimales() {
        Quote q = QuoteCalculator.calculate(new BigDecimal("980000.005"), 2);
        assertThat(q.total().scale()).isEqualTo(2);
    }
}
