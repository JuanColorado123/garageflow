package com.subacol.msauditoria.domain;

import java.time.LocalDateTime;

/**
 * Registro de un cambio de estado de una orden.
 * Validación: eventoId, ordenId, estadoNuevo, usuario, origen y ocurridoEn son obligatorios.
 */
public record CambioDeEstado(
        String eventoId,
        String ordenId,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        String usuario,
        String origen,
        LocalDateTime ocurridoEn
) {
    public CambioDeEstado {
        if (eventoId == null || eventoId.isBlank()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta eventoId"
            );
        }
        if (ordenId == null || ordenId.isBlank()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta ordenId"
            );
        }
        if (estadoNuevo == null || estadoNuevo.isBlank()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta estadoNuevo"
            );
        }
        if (usuario == null || usuario.isBlank()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta usuario"
            );
        }
        if (origen == null || origen.isBlank()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta origen"
            );
        }
        if (ocurridoEn == null) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "EVENTO_INVALIDO",
                    "Al cambio de estado le falta ocurridoEn"
            );
        }
    }
}
