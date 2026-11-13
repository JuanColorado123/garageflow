package com.subacol.msnotificaciones.adapter.in.messaging;

import tools.jackson.databind.json.JsonMapper;
import com.subacol.msnotificaciones.application.port.in.AvisosUseCase;
import com.subacol.msnotificaciones.domain.DatosCita;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CitasListener {

    public static final String COLA_AGENDADA = "notificaciones.cita-agendada";
    public static final String COLA_CANCELADA = "notificaciones.cita-cancelada";

    private final AvisosUseCase avisos;
    private final JsonMapper jsonMapper;

    public CitasListener(AvisosUseCase avisos, JsonMapper jsonMapper) {
        this.avisos = avisos;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = COLA_AGENDADA)
    public void alAgendarse(String mensaje) {
        CitaAgendadaV1 evento = jsonMapper.readValue(mensaje, CitaAgendadaV1.class);
        DatosCita cita = new DatosCita(
                evento.citaId(),
                evento.ordenId(),
                evento.tipoCita(),
                evento.franjaInicio(),
                evento.clienteNombre(),
                evento.clienteTelefono()
        );
        avisos.citaAgendada(evento.eventoId(), cita);
    }

    @RabbitListener(queues = COLA_CANCELADA)
    public void alCancelarse(String mensaje) {
        CitaCanceladaV1 evento = jsonMapper.readValue(mensaje, CitaCanceladaV1.class);
        DatosCita cita = new DatosCita(
                evento.citaId(),
                evento.ordenId(),
                evento.tipoCita(),
                evento.franjaInicio(),
                evento.clienteNombre(),
                evento.clienteTelefono()
        );
        avisos.citaCancelada(evento.eventoId(), cita, evento.motivo());
    }
}
