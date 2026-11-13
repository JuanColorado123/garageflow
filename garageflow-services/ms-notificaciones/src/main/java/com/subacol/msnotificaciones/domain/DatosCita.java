package com.subacol.msnotificaciones.domain;

import java.time.LocalDateTime;

public record DatosCita(
        String citaId,
        String ordenId,
        TipoCita tipoCita,
        LocalDateTime franjaInicio,
        String clienteNombre,
        String clienteTelefono
) {
    public DatosCita {
        if (citaId == null || citaId.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta citaId");
        }
        if (ordenId == null || ordenId.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta ordenId");
        }
        if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta clienteNombre");
        }
        if (clienteTelefono == null || clienteTelefono.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta clienteTelefono");
        }
        if (franjaInicio == null) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta franjaInicio");
        }
    }
}
