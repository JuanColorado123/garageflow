package com.subacol.msnotificaciones.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.subacol.msnotificaciones.domain.TipoCita;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CitaAgendadaV1(
        String eventoId,
        String citaId,
        String ordenId,
        Integer asesorId,
        LocalDateTime franjaInicio,
        TipoCita tipoCita,
        String clienteNombre,
        String clienteTelefono
) {
}
