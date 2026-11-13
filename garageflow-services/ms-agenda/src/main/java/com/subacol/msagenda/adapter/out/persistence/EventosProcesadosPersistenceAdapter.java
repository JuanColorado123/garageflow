package com.subacol.msagenda.adapter.out.persistence;

import com.subacol.msagenda.application.port.out.EventosProcesados;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class EventosProcesadosPersistenceAdapter implements EventosProcesados {
    private final EventoProcesadoJpaRepository repository;
    private final Clock clock;

    public EventosProcesadosPersistenceAdapter(
            EventoProcesadoJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public boolean registrarSiEsNuevo(String eventoId) {
        if (repository.existsById(eventoId)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
        repository.save(new EventoProcesadoEntity(eventoId, now));
        return true;
    }
}
