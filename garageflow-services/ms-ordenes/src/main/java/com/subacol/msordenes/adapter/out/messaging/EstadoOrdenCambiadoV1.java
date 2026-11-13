package com.subacol.msordenes.adapter.out.messaging;

import java.time.LocalDateTime;

public record EstadoOrdenCambiadoV1(
    String eventoId,
    String tipo,
    int version,
    LocalDateTime ocurridoEn,
    String ordenId,
    String estadoAnterior,
    String estadoNuevo,
    String motivo,
    String usuario,
    String origen
) {
}
