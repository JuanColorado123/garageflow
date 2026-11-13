package com.subacol.msnotificaciones.application.service;

import com.subacol.msnotificaciones.application.port.in.AvisosUseCase;
import com.subacol.msnotificaciones.application.port.out.AvisoRepository;
import com.subacol.msnotificaciones.application.port.out.Notificador;
import com.subacol.msnotificaciones.domain.Aviso;
import com.subacol.msnotificaciones.domain.DatosCita;
import com.subacol.msnotificaciones.domain.MotivoCancelacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvisosService implements AvisosUseCase {

    private static final Logger log = LoggerFactory.getLogger(AvisosService.class);

    private final AvisoRepository avisoRepository;
    private final Notificador notificador;

    public AvisosService(AvisoRepository avisoRepository, Notificador notificador) {
        this.avisoRepository = avisoRepository;
        this.notificador = notificador;
    }

    @Override
    @Transactional
    public void citaAgendada(String eventoId, DatosCita cita) {
        Aviso aviso = Aviso.porCitaAgendada(eventoId, cita);
        if (!avisoRepository.registrarSiEsNuevo(aviso)) {
            log.info("Evento repetido {}; no se avisa de nuevo", eventoId);
            return;
        }
        if (aviso.debeEnviarse()) {
            notificador.enviar(aviso);
        }
    }

    @Override
    @Transactional
    public void citaCancelada(String eventoId, DatosCita cita, MotivoCancelacion motivo) {
        Aviso aviso = Aviso.porCitaCancelada(eventoId, cita, motivo);
        if (!avisoRepository.registrarSiEsNuevo(aviso)) {
            log.info("Evento repetido {}; no se avisa de nuevo", eventoId);
            return;
        }
        if (aviso.debeEnviarse()) {
            notificador.enviar(aviso);
        } else {
            log.info("WHATSAPP_OMITIDO cita={} motivo=CIERRE_ORDEN", aviso.citaId());
        }
    }
}
