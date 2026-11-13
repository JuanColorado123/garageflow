package com.subacol.msnotificaciones.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Aviso domain tests")
class AvisoTest {

    private static final String EVENTO_ID = "evento-123";
    private static final String CITA_ID = "cita-456";
    private static final String ORDEN_ID = "orden-789";

    @Test
    @DisplayName("04 REQ-1.1 una cita agendada arma el mensaje con el tipo, la fecha y la hora")
    void testCitaAgendadaMessageFormat() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 17, 10, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Juan García", "3001234567");

        Aviso aviso = Aviso.porCitaAgendada(EVENTO_ID, cita);

        assertThat(aviso.eventoId()).isEqualTo(EVENTO_ID);
        assertThat(aviso.citaId()).isEqualTo(CITA_ID);
        assertThat(aviso.ordenId()).isEqualTo(ORDEN_ID);
        assertThat(aviso.tipoEvento()).isEqualTo(TipoEvento.CITA_AGENDADA);
        assertThat(aviso.telefono()).isEqualTo("3001234567");
        assertThat(aviso.mensaje()).isEqualTo("Hola Juan García, tu cita para traer tu vehículo quedó agendada el 17/11/2026 a las 10:00.");
        assertThat(aviso.estado()).isEqualTo(EstadoAviso.ENVIADO);
        assertThat(aviso.debeEnviarse()).isTrue();
    }

    @Test
    @DisplayName("04 REQ-1.2 una cancelación MANUAL arma el aviso de cancelación")
    void testCitaCanceladaManualMessageFormat() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 20, 14, 30);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.RECOGER, franjaInicio, "María López", "3109876543");

        Aviso aviso = Aviso.porCitaCancelada(EVENTO_ID, cita, MotivoCancelacion.MANUAL);

        assertThat(aviso.eventoId()).isEqualTo(EVENTO_ID);
        assertThat(aviso.citaId()).isEqualTo(CITA_ID);
        assertThat(aviso.ordenId()).isEqualTo(ORDEN_ID);
        assertThat(aviso.tipoEvento()).isEqualTo(TipoEvento.CITA_CANCELADA);
        assertThat(aviso.telefono()).isEqualTo("3109876543");
        assertThat(aviso.mensaje()).isEqualTo("Hola María López, tu cita del 20/11/2026 a las 14:30 fue cancelada.");
        assertThat(aviso.estado()).isEqualTo(EstadoAviso.ENVIADO);
        assertThat(aviso.debeEnviarse()).isTrue();
    }

    @Test
    @DisplayName("04 REQ-1.5 una cita sin teléfono es rechazada con EVENTO_INVALIDO")
    void testCitaSinTelefonoRechazada() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 15, 9, 0);
        
        assertThatThrownBy(() ->
            new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Juan", "")
        ).isInstanceOf(ErrorDeNegocio.class)
         .hasFieldOrPropertyWithValue("codigo", "EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("05 REQ-2.1 una cancelación por CIERRE_ORDEN no se envía")
    void testCitaCanceladaPorCierreOrdenOmitida() {
        LocalDateTime franjaInicio = LocalDateTime.of(2026, 11, 21, 11, 0);
        DatosCita cita = new DatosCita(CITA_ID, ORDEN_ID, TipoCita.TRAER, franjaInicio, "Carlos Ruiz", "3157894561");

        Aviso aviso = Aviso.porCitaCancelada(EVENTO_ID, cita, MotivoCancelacion.CIERRE_ORDEN);

        assertThat(aviso.tipoEvento()).isEqualTo(TipoEvento.CITA_CANCELADA);
        assertThat(aviso.estado()).isEqualTo(EstadoAviso.OMITIDO);
        assertThat(aviso.mensaje()).isNull();
        assertThat(aviso.debeEnviarse()).isFalse();
    }
}
