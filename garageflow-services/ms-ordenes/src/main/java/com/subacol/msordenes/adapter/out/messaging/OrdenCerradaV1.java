package com.subacol.msordenes.adapter.out.messaging;

import java.time.LocalDateTime;

public record OrdenCerradaV1(
    String eventoId,
    String tipo,
    int version,
    LocalDateTime ocurridoEn,
    String ordenId,
    String usuario
) {
}
