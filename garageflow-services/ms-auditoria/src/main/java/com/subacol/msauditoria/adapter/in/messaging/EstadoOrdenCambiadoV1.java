package com.subacol.msauditoria.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * DTO del evento EstadoOrdenCambiado versión 1.
 * Los campos opcionales no rompen la deserialización.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EstadoOrdenCambiadoV1(
        String eventoId,
        String tipo,
        Integer version,
        LocalDateTime ocurridoEn,
        String ordenId,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        String usuario,
        String origen
) {}
