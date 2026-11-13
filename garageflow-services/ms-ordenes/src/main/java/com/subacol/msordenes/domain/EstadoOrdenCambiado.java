package com.subacol.msordenes.domain;

import java.time.LocalDateTime;

public record EstadoOrdenCambiado(
    String ordenId,
    EstadoOrden estadoAnterior,
    EstadoOrden estadoNuevo,
    String motivo,
    String usuario,
    Origen origen,
    LocalDateTime ocurridoEn
) {
}
