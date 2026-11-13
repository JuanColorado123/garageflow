package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.domain.AccionOrden;
import com.subacol.msordenes.domain.EstadoOrden;
import com.subacol.msordenes.domain.Orden;
import com.subacol.msordenes.domain.Origen;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
    String id,
    String clienteNombre,
    String clienteTelefono,
    String vehiculoPlaca,
    String vehiculoDescripcion,
    String aseguradora,
    String trabajo,
    Origen origen,
    EstadoOrden estado,
    EstadoOrden siguienteEstado,
    List<AccionOrden> accionesPermitidas,
    LocalDateTime creadaEn,
    LocalDateTime actualizadaEn
) {
    public static OrdenResponse desde(Orden orden, boolean puedeModificar) {
        return new OrdenResponse(
            orden.id(),
            orden.datos().clienteNombre(),
            orden.datos().clienteTelefono(),
            orden.datos().vehiculoPlaca(),
            orden.datos().vehiculoDescripcion(),
            orden.datos().aseguradora(),
            orden.datos().trabajo(),
            orden.origen(),
            orden.estado(),
            orden.siguienteEstado(),
            puedeModificar ? orden.accionesPermitidas() : List.of(),
            orden.creadaEn(),
            orden.actualizadaEn()
        );
    }
}
