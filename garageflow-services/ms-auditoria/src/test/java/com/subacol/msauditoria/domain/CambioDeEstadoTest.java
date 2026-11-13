package com.subacol.msauditoria.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CambioDeEstadoTest {

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin usuario es rechazado con EVENTO_INVALIDO")
    void sinUsuarioEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                null, // sin usuario
                "SISTEMA",
                LocalDateTime.now()
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("usuario")
                .satisfies(e -> {
                    ErrorDeNegocio error = (ErrorDeNegocio) e;
                    assertThat(error.codigo()).isEqualTo("EVENTO_INVALIDO");
                    assertThat(error.tipo()).isEqualTo(ErrorDeNegocio.Tipo.INVALIDO);
                });
    }

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin eventoId es rechazado con EVENTO_INVALIDO")
    void sinEventoIdEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                null,
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("eventoId")
                .extracting("codigo").isEqualTo("EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin ordenId es rechazado con EVENTO_INVALIDO")
    void sinOrdenIdEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                "evento-1",
                "",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("ordenId")
                .extracting("codigo").isEqualTo("EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin estadoNuevo es rechazado con EVENTO_INVALIDO")
    void sinEstadoNuevoEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                null,
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("estadoNuevo")
                .extracting("codigo").isEqualTo("EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin origen es rechazado con EVENTO_INVALIDO")
    void sinOrigenEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                null,
                LocalDateTime.now()
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("origen")
                .extracting("codigo").isEqualTo("EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("02 REQ-1.3 un cambio sin ocurridoEn es rechazado con EVENTO_INVALIDO")
    void sinOcurridoEnEsRechazado() {
        assertThatThrownBy(() -> new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                null
        ))
                .isInstanceOf(ErrorDeNegocio.class)
                .hasMessageContaining("ocurridoEn")
                .extracting("codigo").isEqualTo("EVENTO_INVALIDO");
    }

    @Test
    @DisplayName("02 REQ-1.1 un cambio valido se crea correctamente")
    void cambioValidoSeCreaCorrectamente() {
        var cambio = new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                "Motor averiado",
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        );
        assertThat(cambio.eventoId()).isEqualTo("evento-1");
        assertThat(cambio.ordenId()).isEqualTo("orden-1");
        assertThat(cambio.estadoAnterior()).isEqualTo("CREADA");
        assertThat(cambio.estadoNuevo()).isEqualTo("EN_PROGRESO");
        assertThat(cambio.motivo()).isEqualTo("Motor averiado");
        assertThat(cambio.usuario()).isEqualTo("asesor.ana");
        assertThat(cambio.origen()).isEqualTo("SISTEMA");
    }
}
