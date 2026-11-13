package com.subacol.msagenda.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cita {
    private final String id;
    private final String ordenId;
    private final Integer asesorId;
    private final LocalDateTime franjaInicio;
    private final TipoCita tipo;
    private EstadoCita estado;
    private MotivoCancelacion motivoCancelacion;
    private final String clienteNombre;
    private final String clienteTelefono;
    private final LocalDateTime creadaEn;
    private LocalDateTime canceladaEn;
    private final List<Object> eventos = new ArrayList<>();

    private Cita(
            String id,
            String ordenId,
            Integer asesorId,
            LocalDateTime franjaInicio,
            TipoCita tipo,
            EstadoCita estado,
            MotivoCancelacion motivoCancelacion,
            String clienteNombre,
            String clienteTelefono,
            LocalDateTime creadaEn,
            LocalDateTime canceladaEn) {
        this.id = id;
        this.ordenId = ordenId;
        this.asesorId = asesorId;
        this.franjaInicio = franjaInicio;
        this.tipo = tipo;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.creadaEn = creadaEn;
        this.canceladaEn = canceladaEn;
    }

    public static Cita agendar(
            String id,
            String ordenId,
            Integer asesorId,
            Franja franja,
            TipoCita tipo,
            String clienteNombre,
            String clienteTelefono,
            LocalDateTime ahora) {
        Cita cita = new Cita(
                id,
                ordenId,
                asesorId,
                franja.inicio(),
                tipo,
                EstadoCita.PENDIENTE,
                null,
                clienteNombre,
                clienteTelefono,
                ahora,
                null);
        cita.eventos.add(
                new CitaAgendada(
                        id, ordenId, asesorId, franja.inicio(), tipo, clienteNombre, clienteTelefono, ahora));
        return cita;
    }

    public static Cita reconstituir(
            String id,
            String ordenId,
            Integer asesorId,
            LocalDateTime franjaInicio,
            TipoCita tipo,
            EstadoCita estado,
            MotivoCancelacion motivoCancelacion,
            String clienteNombre,
            String clienteTelefono,
            LocalDateTime creadaEn,
            LocalDateTime canceladaEn) {
        return new Cita(
                id,
                ordenId,
                asesorId,
                franjaInicio,
                tipo,
                estado,
                motivoCancelacion,
                clienteNombre,
                clienteTelefono,
                creadaEn,
                canceladaEn);
    }

    public void cancelar(MotivoCancelacion motivo, LocalDateTime ahora) {
        if (estado != EstadoCita.PENDIENTE) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.CONFLICTO,
                    "CITA_NO_PENDIENTE",
                    "La cita ya esta cancelada");
        }
        estado = EstadoCita.CANCELADA;
        motivoCancelacion = motivo;
        canceladaEn = ahora;
        eventos.add(
                new CitaCancelada(
                        id,
                        ordenId,
                        asesorId,
                        franjaInicio,
                        tipo,
                        clienteNombre,
                        clienteTelefono,
                        motivo,
                        ahora));
    }

    public List<AccionCita> accionesPermitidas() {
        if (estado == EstadoCita.PENDIENTE) {
            return List.of(AccionCita.CANCELAR);
        }
        return List.of();
    }

    public List<Object> extraerEventos() {
        List<Object> result = new ArrayList<>(eventos);
        eventos.clear();
        return result;
    }

    public String id() {
        return id;
    }

    public String ordenId() {
        return ordenId;
    }

    public Integer asesorId() {
        return asesorId;
    }

    public LocalDateTime franjaInicio() {
        return franjaInicio;
    }

    public TipoCita tipo() {
        return tipo;
    }

    public EstadoCita estado() {
        return estado;
    }

    public MotivoCancelacion motivoCancelacion() {
        return motivoCancelacion;
    }

    public String clienteNombre() {
        return clienteNombre;
    }

    public String clienteTelefono() {
        return clienteTelefono;
    }

    public LocalDateTime creadaEn() {
        return creadaEn;
    }

    public LocalDateTime canceladaEn() {
        return canceladaEn;
    }
}
