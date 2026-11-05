package co.viajesplus.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos colombianos para las vistas: ${@fmt.pesos(valor)}, ${@fmt.fecha(fecha)}. */
@Component("fmt")
public class Fmt {

    private static final Locale CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", CO);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", CO)
            .withZone(ZoneId.of("America/Bogota"));

    public String pesos(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(CO);
        return "$" + nf.format(value);
    }

    public String fecha(LocalDate date) {
        return date == null ? "" : FECHA.format(date);
    }

    public String fechaHora(Instant instant) {
        return instant == null ? "" : FECHA_HORA.format(instant);
    }
}
