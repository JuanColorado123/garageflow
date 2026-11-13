package com.subacol.msagenda.application.port.in;

import com.subacol.msagenda.domain.TipoCita;
import java.time.LocalDateTime;

public record AgendarCitaComando(
        String ordenId, Integer asesorId, LocalDateTime franjaInicio, TipoCita tipo) {
}
