package com.subacol.msagenda.adapter.in.web;

import static org.assertj.core.api.Assertions.*;

import com.subacol.msagenda.domain.AccionCita;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.Franja;
import com.subacol.msagenda.domain.TipoCita;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CitaResponseTest {
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    @Test
    @DisplayName("07 REQ-3.3 sin permiso de modificar no hay acciones")
    void testSinPermisoModificar() {
        LocalDateTime franjaInicio = AHORA.plusDays(1).withHour(10).withMinute(0);
        Franja franja = new Franja(franjaInicio);
        Cita cita =
                Cita.agendar(
                        "cita-1",
                        "orden-1",
                        1,
                        franja,
                        TipoCita.TRAER,
                        "Cliente",
                        "123456",
                        AHORA);

        CitaResponse sinPermiso = CitaResponse.desde(cita, false);
        CitaResponse conPermiso = CitaResponse.desde(cita, true);

        assertThat(sinPermiso.accionesPermitidas()).isEmpty();
        assertThat(conPermiso.accionesPermitidas()).containsExactly(AccionCita.CANCELAR);
    }
}
