package com.subacol.msnotificaciones.adapter.out.notification;

import com.subacol.msnotificaciones.application.port.out.Notificador;
import com.subacol.msnotificaciones.domain.Aviso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppLogNotificador implements Notificador {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppLogNotificador.class);

    @Override
    public void enviar(Aviso aviso) {
        log.info("WHATSAPP_SIMULADO cita={} telefono={} mensaje=\"{}\"", aviso.citaId(), aviso.telefono(), aviso.mensaje());
    }
}
