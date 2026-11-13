package com.subacol.msagenda.adapter.out.messaging;

import java.time.LocalDateTime;

public record CitaAgendadaV1(
        String eventoId,
        String tipo,
        int version,
        LocalDateTime ocurridoEn,
        String citaId,
        String ordenId,
        Integer asesorId,
        LocalDateTime franjaInicio,
        String tipoCita,
        String clienteNombre,
        String clienteTelefono) {
}
