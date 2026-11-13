package com.subacol.msauditoria.adapter.out.persistence;

import com.subacol.msauditoria.application.port.out.HistorialRepository;
import com.subacol.msauditoria.domain.CambioDeEstado;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementa HistorialRepository usando JPA.
 */
@Component
public class HistorialPersistenceAdapter implements HistorialRepository {
    private final HistorialJpaRepository jpaRepository;

    public HistorialPersistenceAdapter(HistorialJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean agregar(CambioDeEstado c) {
        if (jpaRepository.existsByEventoId(c.eventoId())) {
            return false;
        }
        CambioDeEstadoEntity entity = new CambioDeEstadoEntity(
                c.eventoId(),
                c.ordenId(),
                c.estadoAnterior(),
                c.estadoNuevo(),
                c.motivo(),
                c.usuario(),
                c.origen(),
                c.ocurridoEn()
        );
        jpaRepository.save(entity);
        return true;
    }

    @Override
    public List<CambioDeEstado> porOrden(String ordenId) {
        return jpaRepository.findByOrdenIdOrderByOcurridoEnAscIdAsc(ordenId).stream()
                .map(this::entityToDomain)
                .toList();
    }

    @Override
    public List<CambioDeEstado> ultimos(int cantidad) {
        return jpaRepository.findAllByOrderByOcurridoEnDescIdDesc(Limit.of(cantidad)).stream()
                .map(this::entityToDomain)
                .toList();
    }

    private CambioDeEstado entityToDomain(CambioDeEstadoEntity entity) {
        return new CambioDeEstado(
                entity.getEventoId(),
                entity.getOrdenId(),
                entity.getEstadoAnterior(),
                entity.getEstadoNuevo(),
                entity.getMotivo(),
                entity.getUsuario(),
                entity.getOrigen(),
                entity.getOcurridoEn()
        );
    }
}
