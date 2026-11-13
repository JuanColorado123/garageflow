package com.subacol.msagenda.adapter.in.messaging;

import tools.jackson.databind.json.JsonMapper;
import com.subacol.msagenda.application.port.in.AgendaUseCase;
import com.subacol.msagenda.domain.ErrorDeNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrdenCerradaListener {
    private static final Logger log = LoggerFactory.getLogger(OrdenCerradaListener.class);
    public static final String COLA = "agenda.orden-cerrada";
    private final JsonMapper jsonMapper;
    private final AgendaUseCase agenda;

    public OrdenCerradaListener(JsonMapper jsonMapper, AgendaUseCase agenda) {
        this.jsonMapper = jsonMapper;
        this.agenda = agenda;
    }

    @RabbitListener(queues = COLA)
    public void alRecibir(String mensaje) {
        try {
            OrdenCerradaV1 evento = jsonMapper.readValue(mensaje, OrdenCerradaV1.class);
            if ((evento.eventoId() == null || evento.eventoId().isBlank())
                    || (evento.ordenId() == null || evento.ordenId().isBlank())) {
                throw new ErrorDeNegocio(
                        ErrorDeNegocio.Tipo.INVALIDO,
                        "EVENTO_INVALIDO",
                        "El evento no tiene los datos requeridos");
            }
            agenda.alCerrarseOrden(evento.eventoId(), evento.ordenId());
            log.info("Orden {} cerrada, evento {}", evento.ordenId(), evento.eventoId());
        } catch (Exception e) {
            log.error("Error procesando OrdenCerrada", e);
            throw new RuntimeException(e);
        }
    }
}
