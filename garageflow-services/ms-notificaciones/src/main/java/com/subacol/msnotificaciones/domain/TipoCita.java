package com.subacol.msnotificaciones.domain;

public enum TipoCita {
    TRAER("traer"),
    RECOGER("recoger");

    private final String texto;

    TipoCita(String texto) {
        this.texto = texto;
    }

    public String texto() {
        return texto;
    }
}
