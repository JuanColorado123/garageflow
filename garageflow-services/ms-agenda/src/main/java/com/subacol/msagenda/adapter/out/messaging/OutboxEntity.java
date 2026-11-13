package com.subacol.msagenda.adapter.out.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
public class OutboxEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "routing_key", length = 60, nullable = false)
    private String routingKey;

    @Column(length = 4000, nullable = false)
    private String payload;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "publicado_en")
    private LocalDateTime publicadoEn;

    protected OutboxEntity() {}

    public OutboxEntity(
            String id, String routingKey, String payload, LocalDateTime creadoEn) {
        this.id = id;
        this.routingKey = routingKey;
        this.payload = payload;
        this.creadoEn = creadoEn;
    }

    public String getId() {
        return id;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getPublicadoEn() {
        return publicadoEn;
    }

    public void setPublicadoEn(LocalDateTime publicadoEn) {
        this.publicadoEn = publicadoEn;
    }
}
