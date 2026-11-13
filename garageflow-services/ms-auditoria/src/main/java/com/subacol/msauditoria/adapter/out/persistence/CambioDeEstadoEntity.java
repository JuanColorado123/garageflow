package com.subacol.msauditoria.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

/**
 * Entidad JPA para historial_estados (inmutable).
 */
@Entity
@Table(name = "historial_estados")
@Immutable
public class CambioDeEstadoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", length = 36, nullable = false)
    private String eventoId;

    @Column(name = "orden_id", length = 36, nullable = false)
    private String ordenId;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 20, nullable = false)
    private String estadoNuevo;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "usuario", length = 100, nullable = false)
    private String usuario;

    @Column(name = "origen", length = 20, nullable = false)
    private String origen;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;

    protected CambioDeEstadoEntity() {}

    public CambioDeEstadoEntity(
            String eventoId,
            String ordenId,
            String estadoAnterior,
            String estadoNuevo,
            String motivo,
            String usuario,
            String origen,
            LocalDateTime ocurridoEn
    ) {
        this.eventoId = eventoId;
        this.ordenId = ordenId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.motivo = motivo;
        this.usuario = usuario;
        this.origen = origen;
        this.ocurridoEn = ocurridoEn;
    }

    public Long getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getOrdenId() {
        return ordenId;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getOrigen() {
        return origen;
    }

    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
