package com.subacol.msauditoria.adapter.in.messaging;

import com.subacol.msauditoria.application.port.in.HistorialUseCase;
import com.subacol.msauditoria.domain.CambioDeEstado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EstadoOrdenCambiadoListener {
    public static final String COLA = "auditoria.orden-estado-cambiado";
    private static final Logger log = LoggerFactory.getLogger(EstadoOrdenCambiadoListener.class);
    private final HistorialUseCase historial;
    private final JsonMapper jsonMapper;

    public EstadoOrdenCambiadoListener(HistorialUseCase historial, JsonMapper jsonMapper) {
        this.historial = historial;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = COLA)
    public void alRecibir(String mensaje) {
        EstadoOrdenCambiadoV1 evento = jsonMapper.readValue(mensaje, EstadoOrdenCambiadoV1.class);
        CambioDeEstado cambio = new CambioDeEstado(
                evento.eventoId(),
                evento.ordenId(),
                evento.estadoAnterior(),
                evento.estadoNuevo(),
                evento.motivo(),
                evento.usuario(),
                evento.origen(),
                evento.ocurridoEn()
        );
        boolean esNuevo = historial.registrar(cambio);
        if (esNuevo) {
            log.info("Cambio de estado registrado: evento_id={}, orden_id={}", cambio.eventoId(), cambio.ordenId());
        } else {
            log.info("Evento repetido descartado: evento_id={}", cambio.eventoId());
        }
    }
}
