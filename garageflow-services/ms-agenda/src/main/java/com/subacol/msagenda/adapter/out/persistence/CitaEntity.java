package com.subacol.msagenda.adapter.out.persistence;

import com.subacol.msagenda.domain.EstadoCita;
import com.subacol.msagenda.domain.MotivoCancelacion;
import com.subacol.msagenda.domain.TipoCita;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class CitaEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "orden_id", length = 36, nullable = false)
    private String ordenId;

    @Column(name = "asesor_id", nullable = false)
    private Integer asesorId;

    @Column(name = "franja_inicio", nullable = false)
    private LocalDateTime franjaInicio;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TipoCita tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private EstadoCita estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_cancelacion", length = 20)
    private MotivoCancelacion motivoCancelacion;

    @Column(name = "cliente_nombre", length = 120, nullable = false)
    private String clienteNombre;

    @Column(name = "cliente_telefono", length = 30, nullable = false)
    private String clienteTelefono;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "cancelada_en")
    private LocalDateTime canceladaEn;

    protected CitaEntity() {}

    public CitaEntity(
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

    public String getId() {
        return id;
    }

    public String getOrdenId() {
        return ordenId;
    }

    public Integer getAsesorId() {
        return asesorId;
    }

    public LocalDateTime getFranjaInicio() {
        return franjaInicio;
    }

    public TipoCita getTipo() {
        return tipo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public MotivoCancelacion getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public LocalDateTime getCanceladaEn() {
        return canceladaEn;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public void setMotivoCancelacion(MotivoCancelacion motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public void setCanceladaEn(LocalDateTime canceladaEn) {
        this.canceladaEn = canceladaEn;
    }
}
