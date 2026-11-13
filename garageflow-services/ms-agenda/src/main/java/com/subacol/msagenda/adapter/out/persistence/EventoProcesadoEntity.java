package com.subacol.msagenda.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_procesados")
public class EventoProcesadoEntity {
    @Id
    @Column(name = "evento_id", length = 36)
    private String eventoId;

    @Column(name = "procesado_en", nullable = false)
    private LocalDateTime procesadoEn;

    protected EventoProcesadoEntity() {}

    public EventoProcesadoEntity(String eventoId, LocalDateTime procesadoEn) {
        this.eventoId = eventoId;
        this.procesadoEn = procesadoEn;
    }

    public String getEventoId() {
        return eventoId;
    }

    public LocalDateTime getProcesadoEn() {
        return procesadoEn;
    }
}
