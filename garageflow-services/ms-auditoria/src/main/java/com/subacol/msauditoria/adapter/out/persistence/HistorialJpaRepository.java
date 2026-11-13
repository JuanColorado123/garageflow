package com.subacol.msauditoria.adapter.out.persistence;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para historial_estados.
 */
public interface HistorialJpaRepository extends JpaRepository<CambioDeEstadoEntity, Long> {
    /**
     * Verifica si existe un evento con el eventoId dado.
     */
    boolean existsByEventoId(String eventoId);

    /**
     * Busca los cambios de una orden, del más antiguo al más reciente.
     */
    List<CambioDeEstadoEntity> findByOrdenIdOrderByOcurridoEnAscIdAsc(String ordenId);

    /**
     * Busca los últimos cambios de todas las órdenes, del más reciente al más antiguo.
     */
    List<CambioDeEstadoEntity> findAllByOrderByOcurridoEnDescIdDesc(Limit limit);
}
