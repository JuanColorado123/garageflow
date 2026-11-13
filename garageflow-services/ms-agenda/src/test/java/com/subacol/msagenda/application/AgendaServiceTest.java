package com.subacol.msagenda.application;

import static org.assertj.core.api.Assertions.*;

import com.subacol.msagenda.application.port.in.AgendarCitaComando;
import com.subacol.msagenda.application.port.out.AsesorRepository;
import com.subacol.msagenda.application.port.out.CitaRepository;
import com.subacol.msagenda.application.port.out.EventosProcesados;
import com.subacol.msagenda.application.port.out.OrdenInfo;
import com.subacol.msagenda.application.port.out.OrdenesConsulta;
import com.subacol.msagenda.application.port.out.PublicadorEventos;
import com.subacol.msagenda.application.service.AgendaService;
import com.subacol.msagenda.domain.Asesor;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.Disponibilidad;
import com.subacol.msagenda.domain.ErrorDeNegocio;
import com.subacol.msagenda.domain.EstadoCita;
import com.subacol.msagenda.domain.TipoCita;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgendaServiceTest {
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.now(CLOCK);

    private FakeOrdenesConsulta ordenes;
    private FakeAsesorRepository asesores;
    private FakeCitaRepository citas;
    private FakePublicadorEventos publicador;
    private FakeEventosProcesados eventos;
    private AgendaService service;
    private List<String> llamadas;

    @BeforeEach
    void setup() {
        llamadas = new ArrayList<>();
        ordenes = new FakeOrdenesConsulta();
        asesores = new FakeAsesorRepository(llamadas);
        citas = new FakeCitaRepository(llamadas);
        publicador = new FakePublicadorEventos();
        eventos = new FakeEventosProcesados();
        service = new AgendaService(ordenes, asesores, citas, publicador, eventos, CLOCK);
    }

    @Test
    @DisplayName("03 REQ-2.1 agendar con cupo crea la cita PENDIENTE con el contacto del cliente")
    void testAgendarConCupo() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        var cita = service.agendar(cmd);

        assertThat(cita.estado()).isEqualTo(EstadoCita.PENDIENTE);
        assertThat(cita.clienteNombre()).isEqualTo("Juan");
        assertThat(cita.clienteTelefono()).isEqualTo("555-1234");
    }

    @Test
    @DisplayName("03 REQ-2.2 sin cupo responde SIN_CUPO")
    void testAgendarSinCupo() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 1));
        citas.agregarPendiente("orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);

        assertThatThrownBy(() -> service.agendar(cmd))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("SIN_CUPO");
    }

    @Test
    @DisplayName("03 REQ-2.3 bloquea al asesor antes de contar sus citas")
    void testAgendarBloqueaAsesor() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        service.agendar(cmd);

        assertThat(llamadas).containsExactly("bloquear-asesor", "contar");
    }

    @Test
    @DisplayName("03 REQ-1.2 disponibilidad resta las citas pendientes del cupo")
    void testDisponibilidadRestasCitas() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));
        asesores.registrar(new Asesor(2, "Bob", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        service.agendar(cmd);

        var disponibilidades = service.disponibilidad(AHORA.plusDays(1).toLocalDate());

        var manana10 = AHORA.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        Disponibilidad fila1 = disponibilidades.stream()
                .filter(d -> d.asesorId().equals(1) && d.franjaInicio().equals(manana10))
                .findFirst().orElseThrow();
        assertThat(fila1.ocupadas()).isEqualTo(1);
        assertThat(fila1.libres()).isEqualTo(1);
        
        Disponibilidad fila2 = disponibilidades.stream()
                .filter(d -> d.asesorId().equals(2) && d.franjaInicio().equals(manana10))
                .findFirst().orElseThrow();
        assertThat(fila2.ocupadas()).isEqualTo(0);
        assertThat(fila2.libres()).isEqualTo(2);
    }

    @Test
    @DisplayName("03 REQ-2.5 una orden inexistente responde ORDEN_NO_ENCONTRADA")
    void testAgendarOrdenNoExiste() {
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-inexistente", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);

        assertThatThrownBy(() -> service.agendar(cmd))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ORDEN_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("03 REQ-2.6 una orden CERRADA responde ORDEN_CERRADA")
    void testAgendarOrdenCerrada() {
        ordenes.registrar("orden-1", new OrdenInfo("CERRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);

        assertThatThrownBy(() -> service.agendar(cmd))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ORDEN_CERRADA");
    }

    @Test
    @DisplayName("03 REQ-2.7 un asesor inexistente responde ASESOR_NO_ENCONTRADO")
    void testAgendarAsesorNoExiste() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));

        var cmd = new AgendarCitaComando(
                "orden-1", 999, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);

        assertThatThrownBy(() -> service.agendar(cmd))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ASESOR_NO_ENCONTRADO");
    }

    @Test
    @DisplayName("03 REQ-2.8 si ms-ordenes no responde no se crea la cita")
    void testAgendarOrdenesNoDisponible() {
        ordenes.fallar = true;
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);

        assertThatThrownBy(() -> service.agendar(cmd))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("ORDENES_NO_DISPONIBLE");
    }

    @Test
    @DisplayName("03 REQ-3.3 cancelar una cita inexistente responde CITA_NO_ENCONTRADA")
    void testCancelarCitaNoExiste() {
        assertThatThrownBy(() -> service.cancelar("cita-inexistente"))
                .isInstanceOf(ErrorDeNegocio.class)
                .extracting("codigo")
                .isEqualTo("CITA_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("05 REQ-1.1 OrdenCerrada cancela las citas pendientes con CIERRE_ORDEN")
    void testAlCerrarseOrden() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        var citaAgendada = service.agendar(cmd);
        publicador.eventos.clear();

        service.alCerrarseOrden("evento-1", "orden-1");

        var citaActualizada = citas.buscarParaActualizar(citaAgendada.id()).get();
        assertThat(citaActualizada.estado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(citaActualizada.motivoCancelacion()).isEqualTo(com.subacol.msagenda.domain.MotivoCancelacion.CIERRE_ORDEN);
        
        var eventosPublicados = publicador.eventos;
        assertThat(eventosPublicados)
                .anySatisfy(e -> assertThat(e).isInstanceOf(com.subacol.msagenda.domain.CitaCancelada.class));
    }

    @Test
    @DisplayName("05 REQ-1.3 un OrdenCerrada repetido se procesa una sola vez")
    void testAlCerrarseOrdenRepetido() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        service.agendar(cmd);

        service.alCerrarseOrden("evento-1", "orden-1");
        publicador.eventos.clear();

        service.alCerrarseOrden("evento-1", "orden-1");

        assertThat(publicador.eventos).isEmpty();
    }

    @Test
    @DisplayName("05 REQ-1.4 las citas ya canceladas no cambian")
    void testAlCerrarseOrdenCitasCanceladas() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        var cita = service.agendar(cmd);

        cita.cancelar(com.subacol.msagenda.domain.MotivoCancelacion.MANUAL, AHORA);
        citas.guardar(cita);

        service.alCerrarseOrden("evento-1", "orden-1");

        var citaFinal = citas.buscarParaActualizar(cita.id()).get();
        assertThat(citaFinal.motivoCancelacion())
                .isEqualTo(com.subacol.msagenda.domain.MotivoCancelacion.MANUAL);
    }

    @Test
    @DisplayName("05 REQ-1.2 por cada cita cancelada se publica CitaCancelada con CIERRE_ORDEN")
    void testAlCerrarseOrdenPublicaEventos() {
        ordenes.registrar("orden-1", new OrdenInfo("REGISTRADA", "Juan", "555-1234"));
        asesores.registrar(new Asesor(1, "Ana", 2));

        var cmd1 = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(10).withMinute(0), TipoCita.TRAER);
        var cita1 = service.agendar(cmd1);

        var cmd2 = new AgendarCitaComando(
                "orden-1", 1, AHORA.plusDays(1).withHour(11).withMinute(0), TipoCita.RECOGER);
        var cita2 = service.agendar(cmd2);

        publicador.eventos.clear();

        service.alCerrarseOrden("evento-1", "orden-1");

        var eventosPublicados = publicador.eventos;
        assertThat(eventosPublicados)
                .hasSize(2)
                .allSatisfy(e -> assertThat(e).isInstanceOf(com.subacol.msagenda.domain.CitaCancelada.class))
                .anySatisfy(e -> {
                    com.subacol.msagenda.domain.CitaCancelada cc = (com.subacol.msagenda.domain.CitaCancelada) e;
                    assertThat(cc.citaId()).isEqualTo(cita1.id());
                    assertThat(cc.motivo()).isEqualTo(com.subacol.msagenda.domain.MotivoCancelacion.CIERRE_ORDEN);
                })
                .anySatisfy(e -> {
                    com.subacol.msagenda.domain.CitaCancelada cc = (com.subacol.msagenda.domain.CitaCancelada) e;
                    assertThat(cc.citaId()).isEqualTo(cita2.id());
                    assertThat(cc.motivo()).isEqualTo(com.subacol.msagenda.domain.MotivoCancelacion.CIERRE_ORDEN);
                });
    }

    static class FakeOrdenesConsulta implements OrdenesConsulta {
        Map<String, OrdenInfo> ordenes = new HashMap<>();
        boolean fallar = false;

        void registrar(String id, OrdenInfo info) {
            ordenes.put(id, info);
        }

        @Override
        public Optional<OrdenInfo> obtener(String ordenId) {
            if (fallar) {
                throw new ErrorDeNegocio(
                        ErrorDeNegocio.Tipo.NO_DISPONIBLE,
                        "ORDENES_NO_DISPONIBLE",
                        "Error");
            }
            return Optional.ofNullable(ordenes.get(ordenId));
        }
    }

    static class FakeAsesorRepository implements AsesorRepository {
        Map<Integer, Asesor> asesores = new HashMap<>();
        private final List<String> llamadas;

        FakeAsesorRepository(List<String> llamadas) {
            this.llamadas = llamadas;
        }

        void registrar(Asesor asesor) {
            asesores.put(asesor.id(), asesor);
        }

        @Override
        public List<Asesor> listar() {
            return new ArrayList<>(asesores.values());
        }

        @Override
        public Optional<Asesor> buscarYBloquear(Integer id) {
            llamadas.add("bloquear-asesor");
            return Optional.ofNullable(asesores.get(id));
        }
    }

    static class FakeCitaRepository implements CitaRepository {
        Map<String, Cita> citas = new HashMap<>();
        private final List<String> llamadas;

        FakeCitaRepository(List<String> llamadas) {
            this.llamadas = llamadas;
        }

        void agregarPendiente(String ordenId, Integer asesorId, LocalDateTime franjaInicio) {
            Cita cita = Cita.agendar(
                    "cita-fake",
                    ordenId,
                    asesorId,
                    new com.subacol.msagenda.domain.Franja(franjaInicio),
                    TipoCita.TRAER,
                    "Cliente",
                    "123",
                    AHORA);
            citas.put(cita.id(), cita);
        }

        @Override
        public void guardar(Cita c) {
            citas.put(c.id(), c);
        }

        @Override
        public Optional<Integer> asesorDeCita(String citaId) {
            return citas.containsKey(citaId)
                    ? Optional.of(citas.get(citaId).asesorId())
                    : Optional.empty();
        }

        @Override
        public Optional<Cita> buscarParaActualizar(String citaId) {
            return Optional.ofNullable(citas.get(citaId));
        }

        @Override
        public List<Cita> porOrden(String ordenId) {
            return citas.values().stream()
                    .filter(c -> c.ordenId().equals(ordenId))
                    .toList();
        }

        @Override
        public int contarPendientes(Integer asesorId, LocalDateTime franjaInicio) {
            llamadas.add("contar");
            return (int) citas.values().stream()
                    .filter(
                            c -> c.asesorId().equals(asesorId)
                                    && c.franjaInicio().equals(franjaInicio)
                                    && c.estado() == EstadoCita.PENDIENTE)
                    .count();
        }

        @Override
        public List<com.subacol.msagenda.application.port.out.Ocupacion> ocupacionDelDia(
                LocalDate fecha) {
            Map<String, com.subacol.msagenda.application.port.out.Ocupacion> gruposMap = new HashMap<>();
            for (Cita c : citas.values()) {
                if (c.estado() == EstadoCita.PENDIENTE && c.franjaInicio().toLocalDate().equals(fecha)) {
                    String clave = c.asesorId() + "|" + c.franjaInicio();
                    if (gruposMap.containsKey(clave)) {
                        com.subacol.msagenda.application.port.out.Ocupacion existing = gruposMap.get(clave);
                        gruposMap.put(clave, new com.subacol.msagenda.application.port.out.Ocupacion(
                                existing.asesorId(), existing.franjaInicio(), existing.ocupadas() + 1));
                    } else {
                        gruposMap.put(clave, new com.subacol.msagenda.application.port.out.Ocupacion(
                                c.asesorId(), c.franjaInicio(), 1));
                    }
                }
            }
            return new ArrayList<>(gruposMap.values());
        }

        @Override
        public List<Integer> asesoresConPendientes(String ordenId) {
            return citas.values().stream()
                    .filter(c -> c.ordenId().equals(ordenId) && c.estado() == EstadoCita.PENDIENTE)
                    .map(Cita::asesorId)
                    .distinct()
                    .sorted()
                    .toList();
        }

        @Override
        public List<Cita> pendientesDeOrdenParaActualizar(String ordenId) {
            return citas.values().stream()
                    .filter(c -> c.ordenId().equals(ordenId) && c.estado() == EstadoCita.PENDIENTE)
                    .toList();
        }
    }

    static class FakePublicadorEventos implements PublicadorEventos {
        List<Object> eventos = new ArrayList<>();

        @Override
        public void publicar(List<Object> eventos) {
            this.eventos.addAll(eventos);
        }
    }

    static class FakeEventosProcesados implements EventosProcesados {
        private final java.util.Set<String> procesados = new java.util.HashSet<>();

        @Override
        public boolean registrarSiEsNuevo(String eventoId) {
            if (procesados.contains(eventoId)) {
                return false;
            }
            procesados.add(eventoId);
            return true;
        }
    }
}
