package com.subacol.msagenda.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FranjaTest {
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    @Test
    @DisplayName("03 REQ-1.2 delDia devuelve las franjas que no empezaron, de 08:00 a 16:00")
    void testDelDia() {
        LocalDate fecha = AHORA.toLocalDate();
        var franjas = Franja.delDia(fecha, AHORA);

        assertThat(franjas).extracting(f -> f.inicio().getHour()).containsExactly(13, 14, 15, 16);
    }

    @Test
    @DisplayName("03 REQ-1.3 un domingo o una fecha pasada no tiene franjas")
    void testDelDiaSundayOrPast() {
        LocalDate sunday = LocalDate.of(2026, 11, 15);
        var franjas = Franja.delDia(sunday, AHORA);
        assertThat(franjas).isEmpty();

        LocalDate past = AHORA.minusDays(1).toLocalDate();
        franjas = Franja.delDia(past, AHORA);
        assertThat(franjas).isEmpty();
    }

    @Test
    @DisplayName("03 REQ-2.4 una franja que no empieza en punto, fuera de horario, en domingo o pasada es rechazada")
    void testFranjaInvalida() {
        LocalDateTime validFranja = AHORA.plusDays(1).withHour(10).withMinute(0);
        Franja.de(validFranja, AHORA);

        assertThatThrownBy(() -> Franja.de(validFranja.withMinute(30), AHORA))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("FRANJA_INVALIDA");

        assertThatThrownBy(() -> Franja.de(validFranja.withHour(7), AHORA))
                .isInstanceOf(ErrorDeNegocio.class);

        assertThatThrownBy(() -> Franja.de(validFranja.withHour(17), AHORA))
                .isInstanceOf(ErrorDeNegocio.class);

        assertThatThrownBy(() -> Franja.de(null, AHORA))
                .isInstanceOf(ErrorDeNegocio.class);

        assertThatThrownBy(() -> Franja.de(AHORA.minusHours(1), AHORA))
                .isInstanceOf(ErrorDeNegocio.class);
    }
}
