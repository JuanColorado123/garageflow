package com.subacol.msagenda.adapter.in.web;

import com.subacol.msagenda.domain.AccionCita;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.EstadoCita;
import com.subacol.msagenda.domain.MotivoCancelacion;
import com.subacol.msagenda.domain.TipoCita;
import java.time.LocalDateTime;
import java.util.List;

public record CitaResponse(
        String id,
        String ordenId,
        Integer asesorId,
        LocalDateTime franjaInicio,
        TipoCita tipo,
        EstadoCita estado,
        MotivoCancelacion motivoCancelacion,
        String clienteNombre,
        String clienteTelefono,
        LocalDateTime creadaEn,
        List<AccionCita> accionesPermitidas) {

    public static CitaResponse desde(Cita cita, boolean puedeModificar) {
        return new CitaResponse(
                cita.id(),
                cita.ordenId(),
                cita.asesorId(),
                cita.franjaInicio(),
                cita.tipo(),
                cita.estado(),
                cita.motivoCancelacion(),
                cita.clienteNombre(),
                cita.clienteTelefono(),
                cita.creadaEn(),
                puedeModificar ? cita.accionesPermitidas() : List.of());
    }
}
