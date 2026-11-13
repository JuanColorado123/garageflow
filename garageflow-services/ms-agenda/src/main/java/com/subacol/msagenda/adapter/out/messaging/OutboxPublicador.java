package com.subacol.msagenda.adapter.out.messaging;

import tools.jackson.databind.json.JsonMapper;
import com.subacol.msagenda.application.port.out.PublicadorEventos;
import com.subacol.msagenda.domain.CitaAgendada;
import com.subacol.msagenda.domain.CitaCancelada;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

@Component
public class OutboxPublicador implements PublicadorEventos {
    private final OutboxJpaRepository repository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public OutboxPublicador(
            OutboxJpaRepository repository, JsonMapper jsonMapper, Clock clock) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Override
    public void publicar(List<Object> eventos) {
        for (Object evento : eventos) {
            if (evento instanceof CitaAgendada ca) {
                guardarAgendada(ca);
            } else if (evento instanceof CitaCancelada cc) {
                guardarCancelada(cc);
            }
        }
    }

    private void guardarAgendada(CitaAgendada ca) {
        CitaAgendadaV1 v1 =
                new CitaAgendadaV1(
                        UUID.randomUUID().toString(),
                        "CitaAgendada",
                        1,
                        ca.ocurridoEn(),
                        ca.citaId(),
                        ca.ordenId(),
                        ca.asesorId(),
                        ca.franjaInicio(),
                        ca.tipoCita().toString(),
                        ca.clienteNombre(),
                        ca.clienteTelefono());
        guardar(v1.eventoId(), "cita.agendada", v1);
    }

    private void guardarCancelada(CitaCancelada cc) {
        CitaCanceladaV1 v1 =
                new CitaCanceladaV1(
                        UUID.randomUUID().toString(),
                        "CitaCancelada",
                        1,
                        cc.ocurridoEn(),
                        cc.citaId(),
                        cc.ordenId(),
                        cc.asesorId(),
                        cc.franjaInicio(),
                        cc.tipoCita().toString(),
                        cc.clienteNombre(),
                        cc.clienteTelefono(),
                        cc.motivo().toString());
        guardar(v1.eventoId(), "cita.cancelada", v1);
    }

    private void guardar(String eventoId, String routingKey, Object v1) {
        try {
            String payload = jsonMapper.writeValueAsString(v1);
            LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
            repository.save(new OutboxEntity(eventoId, routingKey, payload, now));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
