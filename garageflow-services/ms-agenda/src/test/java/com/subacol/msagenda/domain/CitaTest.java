package com.subacol.msagenda.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CitaTest {
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    @Test
    @DisplayName("03 REQ-3.1 cancelar una cita PENDIENTE la deja CANCELADA con motivo MANUAL")
    void testCancelarPendiente() {
        Cita cita = crearCitaPendiente();
        cita.cancelar(MotivoCancelacion.MANUAL, AHORA);

        assertThat(cita.estado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.motivoCancelacion()).isEqualTo(MotivoCancelacion.MANUAL);
        assertThat(cita.canceladaEn()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("03 REQ-3.2 cancelar una cita CANCELADA es rechazado con CITA_NO_PENDIENTE")
    void testCancelarCancelada() {
        Cita cita = crearCitaPendiente();
        cita.cancelar(MotivoCancelacion.MANUAL, AHORA);

        assertThatThrownBy(() -> cita.cancelar(MotivoCancelacion.MANUAL, AHORA))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("CITA_NO_PENDIENTE");
    }

    @Test
    @DisplayName("03 REQ-4.2 una cita PENDIENTE permite CANCELAR y una CANCELADA no")
    void testAccionesPermitidas() {
        Cita cita = crearCitaPendiente();
        assertThat(cita.accionesPermitidas()).containsExactly(AccionCita.CANCELAR);

        cita.cancelar(MotivoCancelacion.MANUAL, AHORA);
        assertThat(cita.accionesPermitidas()).isEmpty();
    }

    @Test
    @DisplayName("03 REQ-5.1 agendar genera CitaAgendada con sus datos")
    void testAgendarEvent() {
        Franja franja = new Franja(AHORA.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        Cita cita = Cita.agendar(
                "cita-1",
                "orden-1",
                1,
                franja,
                TipoCita.TRAER,
                "Cliente",
                "123456",
                AHORA);

        var eventos = cita.extraerEventos();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0)).isInstanceOf(CitaAgendada.class);

        CitaAgendada ca = (CitaAgendada) eventos.get(0);
        assertThat(ca.citaId()).isEqualTo("cita-1");
        assertThat(ca.ordenId()).isEqualTo("orden-1");
    }

    @Test
    @DisplayName("03 REQ-5.2 cancelar genera CitaCancelada con el motivo")
    void testCancelarEvent() {
        Cita cita = crearCitaPendiente();
        cita.extraerEventos();
        cita.cancelar(MotivoCancelacion.CIERRE_ORDEN, AHORA);

        var eventos = cita.extraerEventos();
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0)).isInstanceOf(CitaCancelada.class);

        CitaCancelada cc = (CitaCancelada) eventos.get(0);
        assertThat(cc.motivo()).isEqualTo(MotivoCancelacion.CIERRE_ORDEN);
    }

    private Cita crearCitaPendiente() {
        Franja franja =
                new Franja(AHORA.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        return Cita.agendar(
                "cita-1",
                "orden-1",
                1,
                franja,
                TipoCita.TRAER,
                "Cliente",
                "123456",
                AHORA);
    }
}
