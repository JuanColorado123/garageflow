package com.subacol.msagenda.application.port.out;

import java.time.LocalDateTime;

public record Ocupacion(Integer asesorId, LocalDateTime franjaInicio, int ocupadas) {
}
