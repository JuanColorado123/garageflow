package com.subacol.msagenda.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "asesores")
public class AsesorEntity {
    @Id private Integer id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "cupo_por_franja", nullable = false)
    private Integer cupoPorFranja;

    protected AsesorEntity() {}

    public AsesorEntity(Integer id, String nombre, Integer cupoPorFranja) {
        this.id = id;
        this.nombre = nombre;
        this.cupoPorFranja = cupoPorFranja;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getCupoPorFranja() {
        return cupoPorFranja;
    }
}
