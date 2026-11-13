package com.subacol.msagenda.application.service;

import com.subacol.msagenda.application.port.in.AgendarCitaComando;
import com.subacol.msagenda.application.port.in.AgendaUseCase;
import com.subacol.msagenda.application.port.out.AsesorRepository;
import com.subacol.msagenda.application.port.out.CitaRepository;
import com.subacol.msagenda.application.port.out.EventosProcesados;
import com.subacol.msagenda.application.port.out.Ocupacion;
import com.subacol.msagenda.application.port.out.OrdenesConsulta;
import com.subacol.msagenda.application.port.out.PublicadorEventos;
import com.subacol.msagenda.domain.Asesor;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.Disponibilidad;
import com.subacol.msagenda.domain.ErrorDeNegocio;
import com.subacol.msagenda.domain.EstadoCita;
import com.subacol.msagenda.domain.Franja;
import com.subacol.msagenda.domain.MotivoCancelacion;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService implements AgendaUseCase {
    private final OrdenesConsulta ordenes;
    private final AsesorRepository asesores;
    private final CitaRepository citas;
    private final PublicadorEventos publicador;
    private final EventosProcesados eventosProcessados;
    private final Clock clock;

    public AgendaService(
            OrdenesConsulta ordenes,
            AsesorRepository asesores,
            CitaRepository citas,
            PublicadorEventos publicador,
            EventosProcesados eventosProcessados,
            Clock clock) {
        this.ordenes = ordenes;
        this.asesores = asesores;
        this.citas = citas;
        this.publicador = publicador;
        this.eventosProcessados = eventosProcessados;
        this.clock = clock;
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    @Transactional
    public Cita agendar(AgendarCitaComando c) {
        LocalDateTime now = ahora();

        Franja franja = Franja.de(c.franjaInicio(), now);

        var ordenInfo = ordenes.obtener(c.ordenId());
        if (ordenInfo.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                    "ORDEN_NO_ENCONTRADA",
                    "La orden no existe");
        }
        if ("CERRADA".equals(ordenInfo.get().estado())) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.CONFLICTO, "ORDEN_CERRADA", "La orden está cerrada");
        }

        var asesor = asesores.buscarYBloquear(c.asesorId());
        if (asesor.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                    "ASESOR_NO_ENCONTRADO",
                    "El asesor no existe");
        }

        int ocupadas = citas.contarPendientes(c.asesorId(), c.franjaInicio());
        if (!asesor.get().tieneCupo(ocupadas)) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.CONFLICTO,
                    "SIN_CUPO",
                    "El asesor no tiene cupo en esa franja");
        }

        Cita nuevaCita = Cita.agendar(
                UUID.randomUUID().toString(),
                c.ordenId(),
                c.asesorId(),
                franja,
                c.tipo(),
                ordenInfo.get().clienteNombre(),
                ordenInfo.get().clienteTelefono(),
                now);
        citas.guardar(nuevaCita);
        publicador.publicar(nuevaCita.extraerEventos());
        return nuevaCita;
    }

    @Override
    @Transactional
    public Cita cancelar(String citaId) {
        LocalDateTime now = ahora();

        var asesorId = citas.asesorDeCita(citaId);
        if (asesorId.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                    "CITA_NO_ENCONTRADA",
                    "La cita no existe");
        }

        asesores.buscarYBloquear(asesorId.get());

        var cita = citas.buscarParaActualizar(citaId);
        if (cita.isEmpty()) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                    "CITA_NO_ENCONTRADA",
                    "La cita no existe");
        }

        cita.get().cancelar(MotivoCancelacion.MANUAL, now);
        citas.guardar(cita.get());
        publicador.publicar(cita.get().extraerEventos());
        return cita.get();
    }

    @Override
    @Transactional
    public void alCerrarseOrden(String eventoId, String ordenId) {
        if (!eventosProcessados.registrarSiEsNuevo(eventoId)) {
            return;
        }

        LocalDateTime now = ahora();
        var asesorIds = citas.asesoresConPendientes(ordenId);
        for (Integer asesorId : asesorIds) {
            asesores.buscarYBloquear(asesorId);
        }

        var citasPendientes = citas.pendientesDeOrdenParaActualizar(ordenId);
        for (Cita cita : citasPendientes) {
            cita.cancelar(MotivoCancelacion.CIERRE_ORDEN, now);
            citas.guardar(cita);
            publicador.publicar(cita.extraerEventos());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Disponibilidad> disponibilidad(LocalDate fecha) {
        LocalDateTime now = ahora();
        List<Franja> franjas = Franja.delDia(fecha, now);
        if (franjas.isEmpty()) {
            return List.of();
        }

        List<Asesor> asesoresList = asesores.listar();
        List<Ocupacion> ocupaciones = citas.ocupacionDelDia(fecha);
        
        Map<String, Integer> ocupadas = new HashMap<>();
        for (Ocupacion o : ocupaciones) {
            ocupadas.put(clave(o.asesorId(), o.franjaInicio()), o.ocupadas());
        }

        List<Disponibilidad> result = new ArrayList<>();
        for (Franja franja : franjas) {
            for (Asesor asesor : asesoresList) {
                int usadas = ocupadas.getOrDefault(clave(asesor.id(), franja.inicio()), 0);
                int libres = Math.max(0, asesor.cupoPorFranja() - usadas);
                result.add(
                        new Disponibilidad(
                                asesor.id(),
                                asesor.nombre(),
                                franja.inicio(),
                                asesor.cupoPorFranja(),
                                usadas,
                                libres));
            }
        }
        return result;
    }

    private static String clave(Integer asesorId, LocalDateTime franjaInicio) {
        return asesorId + "|" + franjaInicio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asesor> asesores() {
        return asesores.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> citasDeOrden(String ordenId) {
        return citas.porOrden(ordenId);
    }
}
