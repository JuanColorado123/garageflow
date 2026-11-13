package com.subacol.msauditoria.adapter.in.web;

import java.util.Locale;
import java.util.Optional;

/**
 * Rol de usuario para el control de acceso simulado.
 */
public enum Rol {
    ASESOR, AUDITOR, ADMIN;

    /**
     * Convierte un string a Rol de forma segura.
     * @param valor el valor a convertir
     * @return un Optional vacío si es null, blank, o no es un rol válido
     */
    public static Optional<Rol> desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Rol.valueOf(valor.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Indica si este rol puede ver el historial general (sin ordenId).
     * @return true para AUDITOR y ADMIN
     */
    public boolean puedeVerHistorialGeneral() {
        return this == AUDITOR || this == ADMIN;
    }
}
