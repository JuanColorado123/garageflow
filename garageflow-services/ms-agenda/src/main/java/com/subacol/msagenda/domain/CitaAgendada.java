package com.subacol.msagenda.domain;

import java.time.LocalDateTime;

public record CitaAgendada(
        String citaId,
        String ordenId,
        Integer asesorId,
        LocalDateTime franjaInicio,
        TipoCita tipoCita,
        String clienteNombre,
        String clienteTelefono,
        LocalDateTime ocurridoEn) {
}
