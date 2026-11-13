package com.subacol.msordenes.adapter.out.messaging;

import com.subacol.msordenes.application.port.out.PublicadorEventos;
import com.subacol.msordenes.domain.EstadoOrdenCambiado;
import com.subacol.msordenes.domain.OrdenCerrada;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxPublicador implements PublicadorEventos {
    private final OutboxJpaRepository jpa;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public OutboxPublicador(OutboxJpaRepository jpa, JsonMapper jsonMapper, Clock clock) {
        this.jpa = jpa;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Override
    public void publicar(List<Object> eventos) {
        for (Object evento : eventos) {
            String eventoId = UUID.randomUUID().toString();
            
            if (evento instanceof EstadoOrdenCambiado e) {
                EstadoOrdenCambiadoV1 dto = new EstadoOrdenCambiadoV1(
                    eventoId,
                    "EstadoOrdenCambiado",
                    1,
                    e.ocurridoEn(),
                    e.ordenId(),
                    e.estadoAnterior() != null ? e.estadoAnterior().name() : null,
                    e.estadoNuevo().name(),
                    e.motivo(),
                    e.usuario(),
                    e.origen().name()
                );
                
                String payload = jsonMapper.writeValueAsString(dto);
                OutboxEntity entity = new OutboxEntity(
                    eventoId,
                    "orden.estado-cambiado",
                    payload,
                    LocalDateTime.now(clock)
                );
                jpa.save(entity);
                
            } else if (evento instanceof OrdenCerrada e) {
                OrdenCerradaV1 dto = new OrdenCerradaV1(
                    eventoId,
                    "OrdenCerrada",
                    1,
                    e.ocurridoEn(),
                    e.ordenId(),
                    e.usuario()
                );
                
                String payload = jsonMapper.writeValueAsString(dto);
                OutboxEntity entity = new OutboxEntity(
                    eventoId,
                    "orden.cerrada",
                    payload,
                    LocalDateTime.now(clock)
                );
                jpa.save(entity);
                
            } else {
                throw new IllegalArgumentException("Tipo de evento no soportado: " + evento.getClass().getName());
            }
        }
    }
}
