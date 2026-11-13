package com.subacol.msauditoria.domain;

/**
 * Excepción que representa un error de regla de negocio.
 */
public class ErrorDeNegocio extends RuntimeException {
    public enum Tipo {
        INVALIDO, NO_ENCONTRADO, CONFLICTO, NO_DISPONIBLE, PROHIBIDO
    }

    private final Tipo tipo;
    private final String codigo;

    public ErrorDeNegocio(Tipo tipo, String codigo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
        this.codigo = codigo;
    }

    public Tipo tipo() {
        return tipo;
    }

    public String codigo() {
        return codigo;
    }
}
