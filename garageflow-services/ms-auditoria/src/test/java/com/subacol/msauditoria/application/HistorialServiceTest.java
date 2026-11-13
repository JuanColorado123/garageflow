package com.subacol.msauditoria.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.subacol.msauditoria.application.port.out.HistorialRepository;
import com.subacol.msauditoria.application.service.HistorialService;
import com.subacol.msauditoria.domain.CambioDeEstado;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HistorialServiceTest {
    private HistorialService service;
    private FakeHistorialRepository fakeRepository;

    @BeforeEach
    void setUp() {
        fakeRepository = new FakeHistorialRepository();
        service = new HistorialService(fakeRepository);
    }

    @Test
    @DisplayName("02 REQ-1.1 registrar guarda el cambio con todos sus datos")
    void registrarGuardaCambio() {
        CambioDeEstado cambio = new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                "Motor averiado",
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.of(2026, 11, 13, 10, 0, 0)
        );

        boolean resultado = service.registrar(cambio);

        assertThat(resultado).isTrue();
        assertThat(fakeRepository.cambiosGuardados).hasSize(1);
        CambioDeEstado guardado = fakeRepository.cambiosGuardados.get(0);
        assertThat(guardado.eventoId()).isEqualTo("evento-1");
        assertThat(guardado.ordenId()).isEqualTo("orden-1");
        assertThat(guardado.estadoAnterior()).isEqualTo("CREADA");
        assertThat(guardado.estadoNuevo()).isEqualTo("EN_PROGRESO");
        assertThat(guardado.motivo()).isEqualTo("Motor averiado");
        assertThat(guardado.usuario()).isEqualTo("asesor.ana");
        assertThat(guardado.origen()).isEqualTo("SISTEMA");
    }

    @Test
    @DisplayName("02 REQ-1.2 un evento repetido se guarda una sola vez")
    void eventoRepetidoNoSeGuardaDosVeces() {
        CambioDeEstado cambio1 = new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        );
        CambioDeEstado cambio2 = new CambioDeEstado(
                "evento-1",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.now()
        );

        boolean resultado1 = service.registrar(cambio1);
        boolean resultado2 = service.registrar(cambio2);

        assertThat(resultado1).isTrue();
        assertThat(resultado2).isFalse();
        assertThat(fakeRepository.cambiosGuardados).hasSize(1);
    }

    @Test
    @DisplayName("02 REQ-3.1 historialDeOrden devuelve los cambios de una orden")
    void historialDeOrdenDevuelveLosDellaOrden() {
        CambioDeEstado cambio1 = new CambioDeEstado(
                "evento-1",
                "orden-1",
                null,
                "CREADA",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.of(2026, 11, 13, 10, 0, 0)
        );
        CambioDeEstado cambio2 = new CambioDeEstado(
                "evento-2",
                "orden-1",
                "CREADA",
                "EN_PROGRESO",
                null,
                "asesor.ana",
                "SISTEMA",
                LocalDateTime.of(2026, 11, 13, 11, 0, 0)
        );
        fakeRepository.agregarDirecto(cambio1);
        fakeRepository.agregarDirecto(cambio2);

        List<CambioDeEstado> resultado = service.historialDeOrden("orden-1");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).estadoNuevo()).isEqualTo("CREADA");
        assertThat(resultado.get(1).estadoNuevo()).isEqualTo("EN_PROGRESO");
    }

    @Test
    @DisplayName("02 REQ-3.2 ultimosCambios consulta los ultimos 200")
    void ultimosCambiosConsultaUltimos() {
        for (int i = 0; i < 50; i++) {
            CambioDeEstado cambio = new CambioDeEstado(
                    "evento-" + i,
                    "orden-" + i,
                    null,
                    "CREADA",
                    null,
                    "asesor.ana",
                    "SISTEMA",
                    LocalDateTime.now()
            );
            fakeRepository.agregarDirecto(cambio);
        }

        List<CambioDeEstado> resultado = service.ultimosCambios();

        assertThat(resultado).hasSizeLessThanOrEqualTo(200);
        assertThat(fakeRepository.ultimoSolicitado).isEqualTo(200);
    }

    // Fake repository para pruebas
    static class FakeHistorialRepository implements HistorialRepository {
        List<CambioDeEstado> cambiosGuardados = new ArrayList<>();
        int ultimoSolicitado = 0;

        @Override
        public boolean agregar(CambioDeEstado c) {
            if (cambiosGuardados.stream().anyMatch(cb -> cb.eventoId().equals(c.eventoId()))) {
                return false;
            }
            cambiosGuardados.add(c);
            return true;
        }

        @Override
        public List<CambioDeEstado> porOrden(String ordenId) {
            return cambiosGuardados.stream()
                    .filter(c -> c.ordenId().equals(ordenId))
                    .toList();
        }

        @Override
        public List<CambioDeEstado> ultimos(int cantidad) {
            ultimoSolicitado = cantidad;
            return cambiosGuardados.stream()
                    .limit(cantidad)
                    .toList();
        }

        void agregarDirecto(CambioDeEstado c) {
            cambiosGuardados.add(c);
        }
    }
}
