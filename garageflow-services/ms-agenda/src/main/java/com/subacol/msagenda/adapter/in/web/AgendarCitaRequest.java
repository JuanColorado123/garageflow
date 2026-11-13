package com.subacol.msagenda.adapter.in.web;

import com.subacol.msagenda.domain.TipoCita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AgendarCitaRequest(
        @NotBlank(message = "es obligatorio") String ordenId,
        @NotNull(message = "es obligatorio") Integer asesorId,
        @NotNull(message = "es obligatorio") LocalDateTime franjaInicio,
        @NotNull(message = "es obligatorio") TipoCita tipo) {
}
