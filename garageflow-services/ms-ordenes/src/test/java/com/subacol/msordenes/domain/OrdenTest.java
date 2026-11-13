package com.subacol.msordenes.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrdenTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-11-16T15:00:00Z"),
        ZoneOffset.UTC
    );
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    @Test
    @DisplayName("01 REQ-1.1 registrar deja la orden REGISTRADA")
    void registrarDejaLaOrdenRegistrada() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        
        assertThat(orden.estado()).isEqualTo(EstadoOrden.REGISTRADA);
        assertThat(orden.extraerEventos()).hasSize(1);
    }

    @Test
    @DisplayName("01 REQ-1.2 registrar sin placa es rechazado con DATOS_INVALIDOS")
    void registrarSinPlacaEsRechazado() {
        assertThatThrownBy(() -> new DatosOrden("Juan", "3001234567", "", null, null, "Cambio de aceite"))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    @DisplayName("01 REQ-1.3 sin aseguradora queda como particular")
    void sinAseguradoraQuedaComoParticular() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        
        assertThat(datos.aseguradora()).isNull();
    }

    @Test
    @DisplayName("01 REQ-2.1 avanzar recorre los estados en orden")
    void avanzarRecorreEstados() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        orden.avanzar("asesor.ana", AHORA);
        assertThat(orden.estado()).isEqualTo(EstadoOrden.EN_DIAGNOSTICO);

        orden.avanzar("asesor.ana", AHORA);
        assertThat(orden.estado()).isEqualTo(EstadoOrden.EN_REPARACION);

        orden.avanzar("asesor.ana", AHORA);
        assertThat(orden.estado()).isEqualTo(EstadoOrden.REPARADA);

        orden.avanzar("asesor.ana", AHORA);
        assertThat(orden.estado()).isEqualTo(EstadoOrden.CERRADA);
    }

    @Test
    @DisplayName("01 REQ-2.2 avanzar una orden CERRADA es rechazado")
    void avanzarUnaOrdenCerradaEsRechazado() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.extraerEventos();

        assertThatThrownBy(() -> orden.avanzar("asesor.ana", AHORA))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("TRANSICION_INVALIDA");
    }

    @Test
    @DisplayName("01 REQ-3.1 reabrir una orden REPARADA o CERRADA la deja EN_REPARACION")
    void reabrirUnaOrdenReparadaOCerrada() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.extraerEventos();

        orden.reabrir("El vehículo volvió", "asesor.ana", AHORA);
        assertThat(orden.estado()).isEqualTo(EstadoOrden.EN_REPARACION);
    }

    @Test
    @DisplayName("01 REQ-3.2 reabrir sin motivo es rechazado con MOTIVO_REQUERIDO")
    void reabrirSinMotivoEsRechazado() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.extraerEventos();

        assertThatThrownBy(() -> orden.reabrir("", "asesor.ana", AHORA))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("MOTIVO_REQUERIDO");
    }

    @Test
    @DisplayName("01 REQ-3.3 reabrir desde REGISTRADA es rechazado")
    void reabrirDesdeRegistradaEsRechazado() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        assertThatThrownBy(() -> orden.reabrir("Motivo", "asesor.ana", AHORA))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("TRANSICION_INVALIDA");
    }

    @Test
    @DisplayName("01 REQ-4.1 cambiar sin usuario es rechazado con USUARIO_REQUERIDO")
    void cambiarSinUsuarioEsRechazado() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        
        assertThatThrownBy(() -> Orden.registrar("id1", datos, Origen.ASESOR, "", AHORA))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("USUARIO_REQUERIDO");
    }

    @Test
    @DisplayName("01 REQ-5.2 acciones permitidas y siguiente estado según el estado")
    void accionesPermitidas() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        assertThat(orden.accionesPermitidas()).contains(AccionOrden.AVANZAR);
        assertThat(orden.accionesPermitidas()).doesNotContain(AccionOrden.REABRIR);
        assertThat(orden.siguienteEstado()).isEqualTo(EstadoOrden.EN_DIAGNOSTICO);

        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.extraerEventos();

        assertThat(orden.accionesPermitidas()).contains(AccionOrden.AVANZAR, AccionOrden.REABRIR);
        assertThat(orden.siguienteEstado()).isEqualTo(EstadoOrden.CERRADA);
    }

    @Test
    @DisplayName("01 REQ-6.1 cada cambio genera EstadoOrdenCambiado con sus datos")
    void cadaCambioGeneraEvento() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        
        List<Object> eventos = orden.extraerEventos();
        assertThat(eventos).hasSize(1);
        
        EstadoOrdenCambiado evento = (EstadoOrdenCambiado) eventos.get(0);
        assertThat(evento.estadoAnterior()).isNull();
        assertThat(evento.estadoNuevo()).isEqualTo(EstadoOrden.REGISTRADA);
        assertThat(evento.usuario()).isEqualTo("asesor.ana");
        assertThat(evento.origen()).isEqualTo(Origen.ASESOR);
    }

    @Test
    @DisplayName("01 REQ-6.2 pasar a CERRADA genera OrdenCerrada")
    void pasarACerradaGeneraOrdenCerrada() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        Orden orden = Orden.registrar("id1", datos, Origen.ASESOR, "asesor.ana", AHORA);
        orden.extraerEventos();

        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.avanzar("asesor.ana", AHORA);
        orden.extraerEventos();
        orden.avanzar("asesor.ana", AHORA);
        
        List<Object> eventos = orden.extraerEventos();
        assertThat(eventos).hasSize(2);
        assertThat(eventos.get(0)).isInstanceOf(EstadoOrdenCambiado.class);
        assertThat(eventos.get(1)).isInstanceOf(OrdenCerrada.class);
    }
}
