package com.subacol.msordenes.domain;

import java.time.LocalDateTime;

public record OrdenCerrada(
    String ordenId,
    String usuario,
    LocalDateTime ocurridoEn
) {
}
