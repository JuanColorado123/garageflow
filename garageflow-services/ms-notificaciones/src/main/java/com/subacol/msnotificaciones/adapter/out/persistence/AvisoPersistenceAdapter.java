package com.subacol.msnotificaciones.adapter.out.persistence;

import com.subacol.msnotificaciones.application.port.out.AvisoRepository;
import com.subacol.msnotificaciones.domain.Aviso;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class AvisoPersistenceAdapter implements AvisoRepository {

    private final AvisoJpaRepository jpaRepository;
    private final Clock clock;

    public AvisoPersistenceAdapter(AvisoJpaRepository jpaRepository, Clock clock) {
        this.jpaRepository = jpaRepository;
        this.clock = clock;
    }

    @Override
    public boolean registrarSiEsNuevo(Aviso aviso) {
        if (jpaRepository.existsById(aviso.eventoId())) {
            return false;
        }
        AvisoEntity entity = new AvisoEntity(
                aviso.eventoId(),
                aviso.citaId(),
                aviso.ordenId(),
                aviso.tipoEvento(),
                aviso.telefono(),
                aviso.mensaje(),
                aviso.estado(),
                LocalDateTime.now(clock)
        );
        jpaRepository.save(entity);
        return true;
    }
}
