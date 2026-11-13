package com.subacol.msauditoria.adapter.in.web;

import com.subacol.msauditoria.domain.CambioDeEstado;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un cambio de estado.
 */
public record CambioDeEstadoResponse(
        String eventoId,
        String ordenId,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        String usuario,
        String origen,
        LocalDateTime ocurridoEn
) {
    public static CambioDeEstadoResponse desde(CambioDeEstado cambio) {
        return new CambioDeEstadoResponse(
                cambio.eventoId(),
                cambio.ordenId(),
                cambio.estadoAnterior(),
                cambio.estadoNuevo(),
                cambio.motivo(),
                cambio.usuario(),
                cambio.origen(),
                cambio.ocurridoEn()
        );
    }
}
