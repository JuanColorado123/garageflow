package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.domain.AccionOrden;
import com.subacol.msordenes.domain.DatosOrden;
import com.subacol.msordenes.domain.Orden;
import com.subacol.msordenes.domain.Origen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;

class OrdenResponseTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-11-16T15:00:00Z"),
        ZoneOffset.UTC
    );
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    @Test
    @DisplayName("07 REQ-3.3 sin permiso de modificar no hay acciones")
    void sinPermisoDeModificarNoHayAcciones() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);

        OrdenResponse sin = OrdenResponse.desde(orden, false);
        OrdenResponse con = OrdenResponse.desde(orden, true);

        assertThat(sin.accionesPermitidas()).isEmpty();
        assertThat(con.accionesPermitidas()).contains(AccionOrden.AVANZAR);
    }
}
