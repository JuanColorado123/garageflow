package com.subacol.msordenes.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Orden {
    private final String id;
    private final DatosOrden datos;
    private final Origen origen;
    private EstadoOrden estado;
    private final LocalDateTime creadaEn;
    private LocalDateTime actualizadaEn;
    private Long version;
    private final List<Object> eventos = new ArrayList<>();

    private Orden(String id, DatosOrden datos, Origen origen, EstadoOrden estado,
                 LocalDateTime creadaEn, LocalDateTime actualizadaEn, Long version) {
        this.id = id;
        this.datos = datos;
        this.origen = origen;
        this.estado = estado;
        this.creadaEn = creadaEn;
        this.actualizadaEn = actualizadaEn;
        this.version = version;
    }

    public static Orden registrar(String id, DatosOrden datos, Origen origen, String usuario, LocalDateTime ahora) {
        if (usuario == null || usuario.isBlank()) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "USUARIO_REQUERIDO",
                "Falta el usuario (cabecera X-Usuario)"
            );
        }
        
        Orden orden = new Orden(id, datos, origen, EstadoOrden.REGISTRADA, ahora, ahora, null);
        orden.eventos.add(new EstadoOrdenCambiado(
            orden.id,
            null,
            EstadoOrden.REGISTRADA,
            null,
            usuario,
            origen,
            ahora
        ));
        return orden;
    }

    public static Orden reconstituir(String id, DatosOrden datos, Origen origen, EstadoOrden estado,
                                    LocalDateTime creadaEn, LocalDateTime actualizadaEn, Long version) {
        return new Orden(id, datos, origen, estado, creadaEn, actualizadaEn, version);
    }

    public void avanzar(String usuario, LocalDateTime ahora) {
        if (usuario == null || usuario.isBlank()) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "USUARIO_REQUERIDO",
                "Falta el usuario (cabecera X-Usuario)"
            );
        }
        
        if (estado == EstadoOrden.CERRADA) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.CONFLICTO,
                "TRANSICION_INVALIDA",
                "No se puede avanzar una orden CERRADA"
            );
        }

        EstadoOrden estadoAnterior = estado;
        estado = estado.siguiente();
        actualizadaEn = ahora;

        eventos.add(new EstadoOrdenCambiado(
            id,
            estadoAnterior,
            estado,
            null,
            usuario,
            origen,
            ahora
        ));

        if (estado == EstadoOrden.CERRADA) {
            eventos.add(new OrdenCerrada(id, usuario, ahora));
        }
    }

    public void reabrir(String motivo, String usuario, LocalDateTime ahora) {
        if (usuario == null || usuario.isBlank()) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "USUARIO_REQUERIDO",
                "Falta el usuario (cabecera X-Usuario)"
            );
        }
        
        if (motivo == null || motivo.isBlank()) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.INVALIDO,
                "MOTIVO_REQUERIDO",
                "Falta el motivo de la reapertura"
            );
        }

        if (estado != EstadoOrden.REPARADA && estado != EstadoOrden.CERRADA) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.CONFLICTO,
                "TRANSICION_INVALIDA",
                "Solo se puede reabrir una orden REPARADA o CERRADA"
            );
        }

        EstadoOrden estadoAnterior = estado;
        estado = EstadoOrden.EN_REPARACION;
        actualizadaEn = ahora;

        eventos.add(new EstadoOrdenCambiado(
            id,
            estadoAnterior,
            estado,
            motivo.trim(),
            usuario,
            origen,
            ahora
        ));
    }

    public List<AccionOrden> accionesPermitidas() {
        List<AccionOrden> acciones = new ArrayList<>();
        if (estado != EstadoOrden.CERRADA) {
            acciones.add(AccionOrden.AVANZAR);
        }
        if (estado == EstadoOrden.REPARADA || estado == EstadoOrden.CERRADA) {
            acciones.add(AccionOrden.REABRIR);
        }
        return acciones;
    }

    public EstadoOrden siguienteEstado() {
        return estado.siguiente();
    }

    public List<Object> extraerEventos() {
        List<Object> eventosAExtraer = new ArrayList<>(eventos);
        eventos.clear();
        return eventosAExtraer;
    }

    // Accessors
    public String id() {
        return id;
    }

    public DatosOrden datos() {
        return datos;
    }

    public Origen origen() {
        return origen;
    }

    public EstadoOrden estado() {
        return estado;
    }

    public LocalDateTime creadaEn() {
        return creadaEn;
    }

    public LocalDateTime actualizadaEn() {
        return actualizadaEn;
    }

    public Long version() {
        return version;
    }
}
