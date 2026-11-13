package com.subacol.msordenes.adapter.out.persistence;

import com.subacol.msordenes.domain.EstadoOrden;
import com.subacol.msordenes.domain.Origen;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
public class OrdenEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "cliente_nombre", length = 120, nullable = false)
    private String clienteNombre;

    @Column(name = "cliente_telefono", length = 30, nullable = false)
    private String clienteTelefono;

    @Column(name = "vehiculo_placa", length = 15, nullable = false)
    private String vehiculoPlaca;

    @Column(name = "vehiculo_descripcion", length = 120)
    private String vehiculoDescripcion;

    @Column(length = 120)
    private String aseguradora;

    @Column(length = 1000, nullable = false)
    private String trabajo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Origen origen;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoOrden estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn;

    @Version
    private Long version;

    protected OrdenEntity() {
    }

    public OrdenEntity(String id, String clienteNombre, String clienteTelefono,
                     String vehiculoPlaca, String vehiculoDescripcion, String aseguradora,
                     String trabajo, Origen origen, EstadoOrden estado,
                     LocalDateTime creadaEn, LocalDateTime actualizadaEn, Long version) {
        this.id = id;
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.vehiculoPlaca = vehiculoPlaca;
        this.vehiculoDescripcion = vehiculoDescripcion;
        this.aseguradora = aseguradora;
        this.trabajo = trabajo;
        this.origen = origen;
        this.estado = estado;
        this.creadaEn = creadaEn;
        this.actualizadaEn = actualizadaEn;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public String getVehiculoPlaca() {
        return vehiculoPlaca;
    }

    public String getVehiculoDescripcion() {
        return vehiculoDescripcion;
    }

    public String getAseguradora() {
        return aseguradora;
    }

    public String getTrabajo() {
        return trabajo;
    }

    public Origen getOrigen() {
        return origen;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public LocalDateTime getActualizadaEn() {
        return actualizadaEn;
    }

    public Long getVersion() {
        return version;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public void setActualizadaEn(LocalDateTime actualizadaEn) {
        this.actualizadaEn = actualizadaEn;
    }
}
