package com.subacol.msagenda.domain;

public record Asesor(Integer id, String nombre, int cupoPorFranja) {

    public boolean tieneCupo(int ocupadas) {
        return ocupadas < cupoPorFranja;
    }
}
