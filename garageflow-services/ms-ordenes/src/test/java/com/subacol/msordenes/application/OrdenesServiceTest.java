package com.subacol.msordenes.application;

import com.subacol.msordenes.application.port.in.OrdenesUseCase;
import com.subacol.msordenes.application.port.in.RegistrarOrdenComando;
import com.subacol.msordenes.application.port.out.OrdenRepository;
import com.subacol.msordenes.application.port.out.PublicadorEventos;
import com.subacol.msordenes.application.service.OrdenesService;
import com.subacol.msordenes.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class OrdenesServiceTest {
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-11-16T15:00:00Z"),
        ZoneOffset.UTC
    );
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    private OrdenesUseCase useCase;
    private FakeOrdenRepository repository;
    private FakePublicadorEventos publicador;

    @BeforeEach
    void setup() {
        repository = new FakeOrdenRepository();
        publicador = new FakePublicadorEventos();
        useCase = new OrdenesService(repository, publicador, CLOCK);
    }

    @Test
    @DisplayName("01 REQ-1.1 registrar guarda la orden y publica su evento")
    void registrarGuardaYPublica() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        RegistrarOrdenComando comando = new RegistrarOrdenComando(datos, Origen.ASESOR, "asesor.ana");
        
        Orden orden = useCase.registrar(comando);
        
        assertThat(orden.estado()).isEqualTo(EstadoOrden.REGISTRADA);
        assertThat(repository.ordenes).hasSize(1);
        assertThat(publicador.eventosPublicados).hasSize(1);
    }

    @Test
    @DisplayName("01 REQ-2.3 avanzar una orden inexistente responde ORDEN_NO_ENCONTRADA")
    void avanzarInexistente() {
        assertThatThrownBy(() -> useCase.avanzar("no-existe", "asesor.ana"))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("ORDEN_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("01 REQ-2.4 si la orden cambió en paralelo no se publican eventos")
    void siOrdenCambioEnParaleloNoSePublica() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, null, "Cambio de aceite");
        RegistrarOrdenComando comando = new RegistrarOrdenComando(datos, Origen.ASESOR, "asesor.ana");
        Orden orden = useCase.registrar(comando);
        publicador.eventosPublicados.clear();
        
        // Simulate another user changing the order
        repository.fallarProximaActualizacion = true;
        
        assertThatThrownBy(() -> useCase.avanzar(orden.id(), "asesor.ana"))
            .isInstanceOf(ErrorDeNegocio.class)
            .extracting(e -> ((ErrorDeNegocio)e).codigo())
            .isEqualTo("ORDEN_MODIFICADA");
        
        // Verify no events were published on the second call
        assertThat(publicador.eventosPublicados).isEmpty();
    }

    @Test
    @DisplayName("01 REQ-7.2 registrar con origen ASEGURADORA conserva el origen")
    void registrarConOrigenAseguradoras() {
        DatosOrden datos = new DatosOrden("Juan", "3001234567", "ABC123", null, "Seguros XYZ", "Cambio de aceite");
        RegistrarOrdenComando comando = new RegistrarOrdenComando(datos, Origen.ASEGURADORA, "integracion-seguros");
        
        Orden orden = useCase.registrar(comando);
        
        assertThat(orden.origen()).isEqualTo(Origen.ASEGURADORA);
    }

    // Fake implementations for testing
    static class FakeOrdenRepository implements OrdenRepository {
        Map<String, Orden> ordenes = new HashMap<>();
        boolean fallarProximaActualizacion = false;

        @Override
        public void guardarNueva(Orden orden) {
            ordenes.put(orden.id(), orden);
        }

        @Override
        public Optional<Orden> buscar(String id) {
            return Optional.ofNullable(ordenes.get(id));
        }

        @Override
        public List<Orden> listar() {
            return new ArrayList<>(ordenes.values());
        }

        @Override
        public boolean actualizar(Orden orden) {
            if (fallarProximaActualizacion) {
                fallarProximaActualizacion = false;
                return false;
            }
            ordenes.put(orden.id(), orden);
            return true;
        }
    }

    static class FakePublicadorEventos implements PublicadorEventos {
        List<Object> eventosPublicados = new ArrayList<>();

        @Override
        public void publicar(List<Object> eventos) {
            eventosPublicados.addAll(eventos);
        }
    }
}
