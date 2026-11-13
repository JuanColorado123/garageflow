package com.subacol.msauditoria.application.port.in;

import com.subacol.msauditoria.domain.CambioDeEstado;
import java.util.List;

/**
 * Caso de uso: registrar cambios de estado y consultar el historial.
 */
public interface HistorialUseCase {
    /**
     * Registra un cambio de estado.
     * @return true si es un evento nuevo, false si ya estaba registrado (eventoId repetido)
     */
    boolean registrar(CambioDeEstado c);

    /**
     * Consulta el historial de una orden, del más antiguo al más reciente.
     */
    List<CambioDeEstado> historialDeOrden(String ordenId);

    /**
     * Consulta los últimos 200 cambios de todas las órdenes, del más reciente al más antiguo.
     */
    List<CambioDeEstado> ultimosCambios();
}
