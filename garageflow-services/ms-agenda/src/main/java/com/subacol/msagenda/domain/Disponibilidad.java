package com.subacol.msagenda.domain;

import java.time.LocalDateTime;

public record Disponibilidad(
        Integer asesorId,
        String asesorNombre,
        LocalDateTime franjaInicio,
        int cupo,
        int ocupadas,
        int libres) {
}
