package com.subacol.msauditoria.application.port.out;

import com.subacol.msauditoria.domain.CambioDeEstado;
import java.util.List;

/**
 * Puerto: persistencia del historial de cambios de estado.
 */
public interface HistorialRepository {
    /**
     * Agrega un cambio de estado.
     * @return true si es nuevo, false si el eventoId ya existe
     */
    boolean agregar(CambioDeEstado c);

    /**
     * Consulta el historial de una orden.
     */
    List<CambioDeEstado> porOrden(String ordenId);

    /**
     * Consulta los últimos cambios.
     */
    List<CambioDeEstado> ultimos(int cantidad);
}
