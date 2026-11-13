package com.subacol.msnotificaciones.adapter.out.persistence;

import com.subacol.msnotificaciones.domain.EstadoAviso;
import com.subacol.msnotificaciones.domain.TipoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "avisos")
public class AvisoEntity {

    @Id
    @Column(name = "evento_id", length = 36)
    private String eventoId;

    @Column(name = "cita_id", length = 36)
    private String citaId;

    @Column(name = "orden_id", length = 36)
    private String ordenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 20)
    private TipoEvento tipoEvento;

    @Column(length = 30)
    private String telefono;

    @Column(length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EstadoAviso estado;

    @Column(name = "registrado_en")
    private LocalDateTime registradoEn;

    protected AvisoEntity() {
    }

    public AvisoEntity(String eventoId, String citaId, String ordenId, TipoEvento tipoEvento,
                       String telefono, String mensaje, EstadoAviso estado, LocalDateTime registradoEn) {
        this.eventoId = eventoId;
        this.citaId = citaId;
        this.ordenId = ordenId;
        this.tipoEvento = tipoEvento;
        this.telefono = telefono;
        this.mensaje = mensaje;
        this.estado = estado;
        this.registradoEn = registradoEn;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getCitaId() {
        return citaId;
    }

    public String getOrdenId() {
        return ordenId;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoAviso getEstado() {
        return estado;
    }

    public LocalDateTime getRegistradoEn() {
        return registradoEn;
    }
}
