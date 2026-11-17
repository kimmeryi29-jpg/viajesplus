package co.viajesplus.infrastructure.config;

import co.viajesplus.application.EmailSender;
import co.viajesplus.infrastructure.persistence.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Correo simulado del entorno de pruebas: el aviso queda guardado y visible en «Avisos». El log registra solo el
 * id del aviso, nunca el destinatario ni el contenido (Ley 1581, Constitución 6).
 */
@Component
public class SimulatedEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SimulatedEmailSender.class);

    @Override
    public boolean send(Notification notification) {
        log.info("Aviso {} entregado al buzón simulado", notification.getId());
        return true;
    }
}
