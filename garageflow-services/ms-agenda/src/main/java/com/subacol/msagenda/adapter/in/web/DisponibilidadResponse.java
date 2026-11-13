package com.subacol.msagenda.adapter.in.web;

import java.time.LocalDateTime;

public record DisponibilidadResponse(
        Integer asesorId,
        String asesorNombre,
        LocalDateTime franjaInicio,
        int cupo,
        int ocupadas,
        int libres) {
}
